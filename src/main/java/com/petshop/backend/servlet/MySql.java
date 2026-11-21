package com.petshop.backend.servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Abre conexões JDBC usando somente configuração externa ao código.
 *
 * <p>Ordem de leitura do ambiente, sempre com {@code trim()} para descartar
 * espaços/quebras de linha invisíveis que o painel do Render costuma colar no
 * valor da variável:</p>
 * <ol>
 *   <li>{@code SMARTPET_DB_URL}, {@code SMARTPET_DB_USER} e
 *       {@code SMARTPET_DB_PASSWORD};</li>
 *   <li>para o que faltar: {@code DB_HOST}, {@code DB_PORT}, {@code DB_SCHEMA},
 *       {@code DB_SSL}, {@code DB_USER} e {@code DB_PASS}.</li>
 * </ol>
 *
 * <p>Fora de {@code localhost} a URL final recebe obrigatoriamente
 * {@code sslMode=REQUIRED}, {@code allowPublicKeyRetrieval=true} e
 * {@code serverTimezone=UTC} — o mínimo exigido pelo Aiven MySQL. Um modo SSL
 * mais forte já presente na URL é preservado, nunca rebaixado.</p>
 */
public final class MySql {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String JDBC_PREFIX = "jdbc:mysql://";
    private static final String LOCAL_URL = "jdbc:mysql://localhost:3306/smartpet?serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_SCHEMA = "smartpet";
    /** Do mais fraco para o mais forte: usado para nunca rebaixar um modo já configurado. */
    private static final List<String> SSL_MODES =
            List.of("DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY");
    private static final long STARTED_MS = System.currentTimeMillis();

    private MySql() { }

    public static Connection connect() throws SQLException {
        Config config = config();
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is missing from the application.", e);
        }
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    /** Configuração efetiva; separada para permitir teste sem abrir conexão. */
    static Config config() {
        String url = env("SMARTPET_DB_URL");
        String user = env("SMARTPET_DB_USER");
        String password = env("SMARTPET_DB_PASSWORD");
        String urlSource = url.isEmpty() ? "" : "SMARTPET_DB_URL";
        String userSource = user.isEmpty() ? "" : "SMARTPET_DB_USER";
        String passwordSource = password.isEmpty() ? "" : "SMARTPET_DB_PASSWORD";

        // Fallback: mesmas credenciais do .env, sob os nomes DB_*.
        if (url.isEmpty()) {
            url = buildUrl(env("DB_HOST"), env("DB_PORT"), env("DB_SCHEMA"));
            if (!url.isEmpty()) {
                urlSource = "DB_HOST";
            }
        }
        if (url.isEmpty()) {
            url = LOCAL_URL;
            urlSource = "padrao-localhost";
        }
        if (user.isEmpty()) {
            user = env("DB_USER");
            if (!user.isEmpty()) {
                userSource = "DB_USER";
            }
        }
        if (user.isEmpty()) {
            user = DEFAULT_USER;
            userSource = "padrao-root";
        }
        if (password.isEmpty()) {
            password = env("DB_PASS");
            if (!password.isEmpty()) {
                passwordSource = "DB_PASS";
            }
        }
        if (password.isEmpty()) {
            passwordSource = "vazia";
        }
        return new Config(normalize(url, env("DB_SSL")), user, password, urlSource, userSource, passwordSource);
    }

    /** Aplica os parâmetros de TLS/timezone exigidos pelo banco remoto. */
    private static String normalize(String url, String dbSsl) {
        String value = url.trim();
        if (value.regionMatches(true, 0, "mysql://", 0, "mysql://".length())) {
            value = "jdbc:" + value; // alguém colou a URL sem o prefixo jdbc:
        }
        if (value.isEmpty()) {
            value = LOCAL_URL;
        }
        if (!value.regionMatches(true, 0, JDBC_PREFIX, 0, JDBC_PREFIX.length())) {
            return value; // formato inesperado: devolve como veio
        }

        int queryStart = value.indexOf('?');
        String base = queryStart < 0 ? value : value.substring(0, queryStart);
        String query = queryStart < 0 ? "" : value.substring(queryStart + 1);

        Map<String, String> params = parseParams(query);
        boolean local = isLocalHost(base.substring(JDBC_PREFIX.length()));
        String sslMode = stronger(normalizeSsl(dbSsl), get(params, "sslMode"));

        if (local) {
            if (sslMode != null) {
                set(params, "sslMode", sslMode);
            }
            putIfAbsent(params, "allowPublicKeyRetrieval", "true");
            putIfAbsent(params, "serverTimezone", "UTC");
        } else {
            set(params, "sslMode", atLeastRequired(sslMode));
            set(params, "allowPublicKeyRetrieval", "true");
            set(params, "serverTimezone", "UTC");
        }
        return base + '?' + joinParams(params);
    }

    /** Monta a URL a partir das variáveis DB_*. Devolve "" se faltar o host. */
    private static String buildUrl(String host, String port, String schema) {
        String h = host;
        if (h.isEmpty()) {
            return "";
        }
        boolean hostHasPort = h.startsWith("[") ? h.contains("]:") : h.contains(":");
        if (!hostHasPort) {
            h = h + ':' + (port.isEmpty() ? DEFAULT_PORT : port);
        }
        String db = schema;
        while (db.startsWith("/")) {
            db = db.substring(1);
        }
        if (db.isEmpty()) {
            db = DEFAULT_SCHEMA;
        }
        return JDBC_PREFIX + h + '/' + db;
    }

    /** true para localhost/IP local; o host vem em "host:porta/base". */
    private static boolean isLocalHost(String hostAndPath) {
        int slash = hostAndPath.indexOf('/');
        String hostAndPort = slash < 0 ? hostAndPath : hostAndPath.substring(0, slash);
        int at = hostAndPort.lastIndexOf('@');
        if (at >= 0) {
            hostAndPort = hostAndPort.substring(at + 1);
        }
        String host = hostAndPort;
        if (host.startsWith("[")) {
            int end = host.indexOf(']');
            if (end > 0) {
                host = host.substring(1, end);
            }
        } else {
            int colon = host.indexOf(':');
            if (colon >= 0) {
                host = host.substring(0, colon);
            }
        }
        host = host.toLowerCase(Locale.ROOT);
        return host.isEmpty() || "localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host);
    }

    /** Impressão SHA-256 curta da senha (sem revelar o valor), só para diagnóstico. */
    static String fingerprint(String password) {
        if (password.isEmpty()) {
            return "";
        }
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Extrai "host:porta" da URL JDBC, sem usuário/senha, para diagnóstico. */
    static String hostOf(String url) {
        int scheme = url.indexOf("//");
        if (scheme < 0) {
            return "";
        }
        int start = scheme + 2;
        int end = url.indexOf('/', start);
        String hostAndPort = end < 0 ? url.substring(start) : url.substring(start, end);
        int at = hostAndPort.lastIndexOf('@');
        return at < 0 ? hostAndPort : hostAndPort.substring(at + 1);
    }

    private static Map<String, String> parseParams(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = (eq < 0 ? pair : pair.substring(0, eq)).trim();
            String val = eq < 0 ? "" : pair.substring(eq + 1).trim();
            if (!key.isEmpty()) {
                set(params, key, val);
            }
        }
        return params;
    }

    private static String joinParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    /** Lê e remove qualquer variante de caixa da chave, gravando a forma canônica. */
    private static void set(Map<String, String> params, String key, String value) {
        params.keySet().removeIf(k -> k.equalsIgnoreCase(key));
        params.put(key, value);
    }

    private static void putIfAbsent(Map<String, String> params, String key, String value) {
        if (get(params, key) == null) {
            set(params, key, value);
        }
    }

    private static String get(Map<String, String> params, String key) {
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    /** Aceita "REQUIRED", "true"/"false", etc. Devolve null quando não reconhece. */
    private static String normalizeSsl(String raw) {
        String v = raw.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) {
            return null;
        }
        switch (v) {
            case "TRUE": case "1": case "YES": case "ON":
                return "REQUIRED";
            case "FALSE": case "0": case "NO": case "OFF":
                return "DISABLED";
            default:
                return SSL_MODES.contains(v) ? v : null;
        }
    }

    private static String atLeastRequired(String mode) {
        return index(mode) < index("REQUIRED") ? "REQUIRED" : mode;
    }

    private static String stronger(String a, String b) {
        int ia = index(a);
        int ib = index(b);
        if (ia < 0) {
            return ib < 0 ? null : b;
        }
        if (ib < 0) {
            return a;
        }
        return ia >= ib ? a : b;
    }

    private static int index(String mode) {
        return mode == null ? -1 : SSL_MODES.indexOf(mode.toUpperCase(Locale.ROOT));
    }

    /**
     * Lê do ambiente e remove o que o painel costuma colar junto: espaços,
     * quebras de linha, BOM invisível e aspas em volta do valor.
     */
    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (!v.isEmpty() && v.charAt(0) == '\uFEFF') {
            v = v.substring(1).trim();
        }
        while (v.length() >= 2) {
            char first = v.charAt(0);
            char last = v.charAt(v.length() - 1);
            boolean quoted = (first == '"' && last == '"') || (first == '\'' && last == '\'');
            if (!quoted) {
                break;
            }
            v = v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    /** Credenciais resolvidas; a senha nunca aparece em log/toString. */
    record Config(String url, String user, String password,
                  String urlSource, String userSource, String passwordSource) {

        /** Nomes das variáveis usadas (sem valores), para diagnóstico. */
        Map<String, String> fontes() {
            return Map.of(
                    "url", urlSource,
                    "usuario", userSource,
                    "senha", passwordSource,
                    "host", hostOf(url),
                    "senhaLen", String.valueOf(password.length()),
                    "senhaHash", fingerprint(password),
                    "uptimeS", String.valueOf((System.currentTimeMillis() - STARTED_MS) / 1000));
        }

        @Override
        public String toString() {
            return "Config[url=" + url + ", user=" + user + " (" + userSource + ")"
                    + ", password=" + (password.isEmpty() ? "<vazia>" : "***")
                    + " (" + passwordSource + ")]";
        }
    }
}
