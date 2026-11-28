package com.petshop.backend.servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/")
public class SmartPetServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Compara CPFs com ou sem pontuação sem alterar a chave já armazenada. */
    private static String cpfSemMascaraSql(String coluna) {
        return "REPLACE(REPLACE(REPLACE(TRIM(" + coluna + "), '.', ''), '-', ''), ' ', '')";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null || path.isEmpty())
            path = req.getServletPath();
        if (path == null || path.isEmpty())
            path = "/";
        req.setAttribute("menuAtivo", path);
        try {
            if ("/health/db".equals(path)) {
                try {
                    query("SELECT 1");
                    json(resp, 200, Map.of("ok", true));
                } catch (SQLException e) {
                    json(resp, 503, Map.of("ok", false, "erro", String.valueOf(e.getMessage()), "sqlState", String.valueOf(e.getSQLState()), "code", e.getErrorCode(), "fonte", MySql.config().fontes()));
                }
                return;
            }
            if (path.startsWith("/css/")) {
                var defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) {
                    defaultServlet.forward(req, resp);
                    return;
                }
            }
            if ("/api/servicos".equals(path)) {
                json(resp, 200, Map.of("sucesso", true, "servicos", query(
                        "SELECT codigo_servico AS id, codigo_servico, nome, preco, categoria FROM servico ORDER BY nome")));
                return;
            }
            if ("/api/produtos".equals(path)) {
                json(resp, 200, query(
                        "SELECT id_produto AS codigo, nome, marca, tipo AS categoria, estoque, preco, url FROM produto ORDER BY nome"));
                return;
            }
            if ("/api/notificacoes".equals(path)) {
                json(resp, 200, List.of());
                return;
            }
            if ("/api/fila".equals(path)) {
                json(resp, 200, filaCompleta());
                return;
            }
            if (path.startsWith("/api/fila/cliente/")) {
                String cpf = Validacao.somenteDigitos(path.substring("/api/fila/cliente/".length()));
                json(resp, 200, query(
                        "SELECT f.id_atendimento AS id,p.nome AS nomePet,s.nome AS servico,f.status,TIMESTAMPDIFF(MINUTE,f.data_hora_entrada,CURRENT_TIMESTAMP) AS esperaMinutos FROM fila_atendimento f JOIN pet p ON p.codigo_pet=f.id_petFK JOIN servico s ON s.codigo_servico=f.id_servicoFK JOIN tutela tu ON tu.codigo_pet=p.codigo_pet WHERE " + cpfSemMascaraSql("tu.cpf") + "=? ORDER BY f.data_hora_entrada DESC",
                        cpf));
                return;
            }
            if (path.startsWith("/api/agendamentos/cliente/")) {
                String cpf = Validacao.somenteDigitos(path.substring("/api/agendamentos/cliente/".length()));
                json(resp, 200, query(
                        "SELECT a.id_agenda AS id,p.nome AS nomePet,s.nome AS servico,s.categoria AS categoria,DATE_FORMAT(a.data_agendamento,'%Y-%m-%d') AS data_agendamento,TIME_FORMAT(a.hora_agendamento,'%H:%i') AS hora_agendamento,a.status FROM agenda a LEFT JOIN pet p ON p.codigo_pet=a.codigo_pet LEFT JOIN servico s ON s.codigo_servico=a.codigo_servico WHERE " + cpfSemMascaraSql("a.cpf") + "=? ORDER BY a.data_agendamento DESC,a.hora_agendamento DESC",
                        cpf));
                return;
            }
            if ("/api/avaliacoes/pendentes".equals(path)) {
                String cpf = Validacao.somenteDigitos(req.getParameter("cpf"));
                if (cpf.isBlank()) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF do tutor obrigatório."));
                    return;
                }
                json(resp, 200, query(
                        "SELECT f.id_atendimento AS id,f.id_atendimento AS servico_id,s.nome AS servico,p.nome AS nomePet,p.codigo_pet AS pet_id FROM fila_atendimento f JOIN servico s ON s.codigo_servico=f.id_servicoFK JOIN pet p ON p.codigo_pet=f.id_petFK JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN avaliacao a ON a.id_atendimentoFK=f.id_atendimento WHERE f.status IN ('Concluído','Concluido','Finalizado') AND a.id_avaliacao IS NULL AND " + cpfSemMascaraSql("tu.cpf") + "=? ORDER BY f.data_hora_entrada DESC",
                        cpf));
                return;
            }
            if (path.startsWith("/tutor/") && path.endsWith("/pets")) {
                String cpf = Validacao.somenteDigitos(
                        path.substring("/tutor/".length(), path.length() - "/pets".length()));
                json(resp, 200, Map.of("sucesso", true, "pets", query(
                        "SELECT p.codigo_pet AS id, p.codigo_pet AS codigoPet, p.nome, p.especie, p.raca, p.idade, p.porte, p.peso, p.n_documento FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE " + cpfSemMascaraSql("t.cpf") + "=? ORDER BY p.nome",
                        cpf)));
                return;
            }
            if ("/login".equals(path) || "/".equals(path)) {
                req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
                return;
            }
            if ("/logout".equals(path)) {
                if (req.getSession(false) != null)
                    req.getSession(false).invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            if ("/clientes".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("clientes",
                        query("SELECT cpf, nome, usuario, telefone, email, endereco FROM tutor ORDER BY nome"));
                req.getRequestDispatcher("/WEB-INF/clientes.jsp").forward(req, resp);
                return;
            }
            if ("/pets".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("pets", query(
                        "SELECT p.codigo_pet AS id, p.nome, p.especie, p.raca, p.idade, t.cpf AS cpfCliente, t.nome AS nomeCliente FROM pet p LEFT JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN tutor t ON t.cpf=tu.cpf ORDER BY p.nome"));
                req.setAttribute("clientes", query("SELECT cpf,nome FROM tutor ORDER BY nome"));
                req.getRequestDispatcher("/WEB-INF/pets.jsp").forward(req, resp);
                return;
            }
            if ("/loja".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("produtos", query(
                        "SELECT id_produto AS codigo,nome,marca,tipo AS categoria,estoque,preco,url FROM produto ORDER BY nome"));
                req.getRequestDispatcher("/WEB-INF/loja.jsp").forward(req, resp);
                return;
            }
            if ("/avaliacoes".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("avaliacoes", query(
                        "SELECT a.id_avaliacao AS id, a.nota, a.comentario, a.id_atendimentoFK AS atendimento, p.nome AS nomePet, s.nome AS servico FROM avaliacao a LEFT JOIN fila_atendimento f ON f.id_atendimento=a.id_atendimentoFK LEFT JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN servico s ON s.codigo_servico=f.id_servicoFK ORDER BY a.id_avaliacao DESC"));
                req.getRequestDispatcher("/WEB-INF/avaliacoes.jsp").forward(req, resp);
                return;
            }
            if ("/fila".equals(path) || "/FilaServlet".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("fila", query(
                        "SELECT f.id_atendimento AS id, f.criticidade AS servico, f.status, f.data_hora_entrada AS horarioChegada, p.nome AS nomePet, t.nome AS nomeCliente, (SELECT COUNT(*) FROM fila_atendimento anterior WHERE anterior.status='Em espera' AND anterior.id_atendimento<=f.id_atendimento) AS posicao, CASE WHEN f.status IN ('Em espera','Em atendimento') THEN TIMESTAMPDIFF(MINUTE,f.data_hora_entrada,CURRENT_TIMESTAMP) ELSE NULL END AS esperaMinutos, CASE WHEN f.status='Em atendimento' THEN 'badge-atendimento' WHEN f.status='Concluído' THEN 'badge-ativo' ELSE 'badge-espera' END AS statusClass FROM fila_atendimento f LEFT JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN tutor t ON t.cpf=tu.cpf ORDER BY f.data_hora_entrada"));
                req.setAttribute("pets", query("SELECT codigo_pet AS id,nome FROM pet ORDER BY nome"));
                req.setAttribute("servicos", query("SELECT codigo_servico AS id,nome FROM servico ORDER BY nome"));
                req.getRequestDispatcher("/WEB-INF/fila.jsp").forward(req, resp);
                return;
            }
            if ("/agendamentos".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("agendamentos", query(
                        "SELECT a.id_agenda AS id, a.data_agendamento AS data, a.hora_agendamento AS hora, a.status, t.nome AS nomeCliente, t.cpf, p.nome AS nomePet, s.nome AS servico FROM agenda a LEFT JOIN tutor t ON t.cpf=a.cpf LEFT JOIN pet p ON p.codigo_pet=a.codigo_pet LEFT JOIN servico s ON s.codigo_servico=a.codigo_servico ORDER BY a.data_agendamento DESC, a.hora_agendamento DESC"));
                req.getRequestDispatcher("/WEB-INF/agendamentos.jsp").forward(req, resp);
                return;
            }
            if ("/admin/agendamentos/excluir".equals(path)) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("DELETE FROM agenda WHERE id_agenda=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("id")));
                    ps.executeUpdate();
                } catch (NumberFormatException e) {
                    resp.sendError(400, "ID inválido.");
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/agendamentos");
                return;
            }
            if ("/balconista".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("fila", filaCompleta());
                req.setAttribute("pets", query("SELECT codigo_pet AS id, nome, especie FROM pet ORDER BY nome"));
                req.setAttribute("servicos",
                        query("SELECT codigo_servico AS id, nome, preco FROM servico ORDER BY nome"));
                flash(req);
                req.getRequestDispatcher("/WEB-INF/balconista.jsp").forward(req, resp);
                return;
            }
            if ("/caixa".equals(path)) {
                if (!admin(req, resp))
                    return;
                List<Map<String, Object>> receitas = query(
                        "SELECT id_receita AS id, categoria, descricao, valor, data_hora FROM receita ORDER BY data_hora DESC, id_receita DESC");
                List<Map<String, Object>> despesas = query(
                        "SELECT id_despesa AS id, categoria, descricao, valor, data_hora FROM despesa ORDER BY data_hora DESC, id_despesa DESC");
                BigDecimal totalReceitas = somar(receitas);
                BigDecimal totalDespesas = somar(despesas);
                req.setAttribute("receitas", receitas);
                req.setAttribute("despesas", despesas);
                req.setAttribute("totalReceitas", totalReceitas);
                req.setAttribute("totalDespesas", totalDespesas);
                req.setAttribute("saldo", totalReceitas.subtract(totalDespesas));
                flash(req);
                req.getRequestDispatcher("/WEB-INF/caixa.jsp").forward(req, resp);
                return;
            }
            if ("/veterinario".equals(path)) {
                if (!admin(req, resp))
                    return;
                req.setAttribute("fila", filaCompleta());
                flash(req);
                req.getRequestDispatcher("/WEB-INF/veterinario.jsp").forward(req, resp);
                return;
            }
            json(resp, 404, Map.of("sucesso", false, "mensagem", "Rota não encontrada."));
        } catch (SQLException e) {
            log("Falha na consulta ao MySQL", e);
            json(resp, 503, Map.of("sucesso", false, "mensagem", "Falha ao consultar o MySQL.", "detalhe", String.valueOf(e.getMessage()), "code", e.getErrorCode(), "sqlState", String.valueOf(e.getSQLState())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null || path.isEmpty())
            path = req.getServletPath();
        if (path == null || path.isEmpty())
            path = "/";
        try {
            if ("/balconista/registrar".equals(path)) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO fila_atendimento(criticidade,data_hora_entrada,status,id_servicoFK,id_petFK) SELECT s.nome,CURRENT_TIMESTAMP,'Em espera',s.codigo_servico,? FROM servico s WHERE s.codigo_servico=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("idPet")));
                    ps.setInt(2, Integer.parseInt(req.getParameter("idServico")));
                    ps.executeUpdate();
                    req.getSession().setAttribute("mensagem", "Pet adicionado à fila de espera.");
                } catch (Exception e) {
                    req.getSession().setAttribute("erro", "Não foi possível adicionar: " + e.getMessage());
                }
                resp.sendRedirect(req.getContextPath() + "/balconista");
                return;
            }
            if ("/fila/acao".equals(path)) {
                if (!admin(req, resp))
                    return;
                String acao = req.getParameter("acao");
                String id = req.getParameter("id");
                try {
                    if ("chamar".equalsIgnoreCase(acao)) {
                        try (Connection c = MySql.connect();
                                PreparedStatement ps = c.prepareStatement(
                                        "UPDATE fila_atendimento SET status='Em atendimento' WHERE status='Em espera' ORDER BY data_hora_entrada LIMIT 1")) {
                            ps.executeUpdate();
                        }
                        req.getSession().setAttribute("mensagem", "Próximo pet chamado para atendimento.");
                    } else if ("concluir".equalsIgnoreCase(acao) && id != null && !id.isBlank()) {
                        try (Connection c = MySql.connect();
                                PreparedStatement ps = c.prepareStatement(
                                        "UPDATE fila_atendimento SET status='Concluído' WHERE id_atendimento=?")) {
                            ps.setInt(1, Integer.parseInt(id));
                            ps.executeUpdate();
                        }
                        req.getSession().setAttribute("mensagem", "Atendimento concluído.");
                    } else if ("cancelar".equalsIgnoreCase(acao) && id != null && !id.isBlank()) {
                        String motivo = req.getParameter("motivo");
                        if (motivo == null || motivo.isBlank())
                            motivo = "Cancelado pela administração";
                        try (Connection c = MySql.connect();
                                PreparedStatement ps = c.prepareStatement(
                                        "UPDATE fila_atendimento SET status='Cancelado', metodo_cancelamento=? WHERE id_atendimento=?")) {
                            ps.setString(1, Validacao.truncar(motivo, 50));
                            ps.setInt(2, Integer.parseInt(id));
                            ps.executeUpdate();
                        }
                        req.getSession().setAttribute("mensagem", "Atendimento cancelado.");
                    }
                } catch (Exception e) {
                    req.getSession().setAttribute("erro", "Não foi possível concluir a ação: " + e.getMessage());
                }
                resp.sendRedirect(req.getContextPath() + voltar(req));
                return;
            }
            if ("/admin/caixa/movimento".equals(path)) {
                if (!admin(req, resp))
                    return;
                String tipo = req.getParameter("tipo");
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("despesa".equals(tipo)
                                ? "INSERT INTO despesa(categoria,descricao,valor,data_hora) VALUES(?,?,?,CURRENT_TIMESTAMP)"
                                : "INSERT INTO receita(categoria,descricao,valor,data_hora) VALUES(?,?,?,CURRENT_TIMESTAMP)")) {
                    ps.setString(1, Validacao.truncar(req.getParameter("categoria"), 50));
                    ps.setString(2, Validacao.truncar(req.getParameter("descricao"), 255));
                    ps.setBigDecimal(3, new BigDecimal(req.getParameter("valor").replace(",", ".")));
                    ps.executeUpdate();
                    req.getSession().setAttribute("mensagem", "Lançamento registrado.");
                } catch (Exception e) {
                    req.getSession().setAttribute("erro", "Não foi possível lançar: " + e.getMessage());
                }
                resp.sendRedirect(req.getContextPath() + "/caixa");
                return;
            }
            if ("/admin/caixa/excluir".equals(path)) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("despesa".equals(req.getParameter("tipo"))
                                ? "DELETE FROM despesa WHERE id_despesa=?"
                                : "DELETE FROM receita WHERE id_receita=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("id")));
                    ps.executeUpdate();
                    req.getSession().setAttribute("mensagem", "Lançamento removido.");
                } catch (Exception e) {
                    req.getSession().setAttribute("erro", "Não foi possível remover: " + e.getMessage());
                }
                resp.sendRedirect(req.getContextPath() + "/caixa");
                return;
            }
            if ("/usuario".equals(path)) {
                Map<String, Object> b = body(req);
                String cpf = value(b, "cpf"), nome = value(b, "nome"), usuario = value(b, "usuario"),
                        senha = String.valueOf(b.getOrDefault("senha", ""));
                if (cpf.isBlank() || nome.isBlank() || usuario.isBlank() || senha.isBlank()) {
                    json(resp, 400,
                            Map.of("sucesso", false, "mensagem", "CPF, nome, usuário e senha são obrigatórios."));
                    return;
                }
                if (!Validacao.cpfValido(cpf)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF inválido: informe os 11 dígitos."));
                    return;
                }
                if (!Validacao.usuarioValido(usuario)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem",
                            "O usuário deve ter de 3 a 50 caracteres e não pode conter espaços."));
                    return;
                }
                if (!Validacao.senhaValida(senha)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "A senha deve ter de 4 a 50 caracteres."));
                    return;
                }
                String email = value(b, "email");
                if (!email.isBlank() && !Validacao.emailValido(email)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "E-mail inválido."));
                    return;
                }
                cpf = Validacao.somenteDigitos(cpf);
                if (!query("SELECT cpf FROM tutor WHERE usuario=? LIMIT 1", usuario).isEmpty()) {
                    json(resp, 409,
                            Map.of("sucesso", false, "mensagem", "Este usuário já está em uso. Escolha outro."));
                    return;
                }
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO tutor(cpf,nome,usuario,senha,telefone,email,endereco,status_conta) VALUES(?,?,?,?,?,?,?,'Ativo')")) {
                    ps.setString(1, cpf);
                    ps.setString(2, Validacao.truncar(nome, 100));
                    ps.setString(3, usuario);
                    ps.setString(4, senha);
                    ps.setString(5, Validacao.truncar(value(b, "telefone"), 20));
                    ps.setString(6, email);
                    ps.setString(7, Validacao.truncar(value(b, "endereco"), 150));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1062) {
                        json(resp, 409, Map.of("sucesso", false, "mensagem", "Já existe um cadastro com este CPF."));
                        return;
                    }
                    throw e;
                }
                json(resp, 201, Map.of("sucesso", true, "mensagem", "Cliente cadastrado."));
                return;
            }
            if ("/login".equals(path)) {
                String user = req.getParameter("usuario"), pass = req.getParameter("senha");
                String adminUser = setting("ADMIN_USER", "adm");
                String adminPass = System.getenv("ADMIN_PASSWORD");
                if (adminPass != null && !adminPass.isBlank() && adminUser.equals(user) && adminPass.equals(pass)) {
                    req.getSession(true).setAttribute("adminLogado", user);
                    resp.sendRedirect(req.getContextPath() + "/fila");
                } else {
                    req.setAttribute("erro",
                            adminPass == null || adminPass.isBlank()
                                    ? "Configure ADMIN_PASSWORD no ambiente do Tomcat para habilitar o acesso."
                                    : "Usuário ou senha inválidos.");
                    req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
                }
                return;
            }
            if ("/admin/pets/salvar".equals(path)) {
                if (!admin(req, resp))
                    return;
                String cpf = req.getParameter("cpfCliente"), nome = req.getParameter("nome");
                String especie = req.getParameter("especie"), raca = req.getParameter("raca");
                int idade = Integer.parseInt(req.getParameter("idade"));
                if (cpf == null || cpf.isBlank() || nome == null || nome.isBlank()) {
                    resp.sendError(400, "Tutor e nome do pet são obrigatórios.");
                    return;
                }
                try (Connection c = MySql.connect()) {
                    c.setAutoCommit(false);
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO pet (nome,especie,raca,idade) VALUES (?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, nome.trim());
                        ps.setString(2, especie);
                        ps.setString(3, raca);
                        ps.setInt(4, idade);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            keys.next();
                            try (PreparedStatement link = c
                                    .prepareStatement("INSERT INTO tutela (codigo_pet,cpf) VALUES (?,?)")) {
                                link.setInt(1, keys.getInt(1));
                                link.setString(2, cpf);
                                link.executeUpdate();
                            }
                        }
                    }
                    c.commit();
                }
                resp.sendRedirect(req.getContextPath() + "/pets");
                return;
            }
            if (path.equals("/admin/clientes/salvar")) {
                if (!admin(req, resp))
                    return;
                String cpf = req.getParameter("cpf"), nome = req.getParameter("nome"),
                        usuario = req.getParameter("novo_usuario"), senha = req.getParameter("nova_senha");
                if (usuario == null)
                    usuario = req.getParameter("usuario");
                if (senha == null)
                    senha = req.getParameter("senha");
                if (cpf == null || nome == null || usuario == null || cpf.isBlank() || nome.isBlank()
                        || usuario.isBlank()) {
                    resp.sendError(400, "CPF, nome e usuário são obrigatórios.");
                    return;
                }
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO tutor(cpf,nome,usuario,senha,telefone,email,endereco,status_conta) VALUES(?,?,?,?,?,?,?,'Ativo') ON DUPLICATE KEY UPDATE nome=VALUES(nome),usuario=VALUES(usuario),senha=IF(VALUES(senha)='',senha,VALUES(senha)),telefone=VALUES(telefone),email=VALUES(email),endereco=VALUES(endereco)")) {
                    ps.setString(1, cpf.trim());
                    ps.setString(2, nome.trim());
                    ps.setString(3, usuario.trim());
                    ps.setString(4, senha == null ? "" : senha);
                    ps.setString(5, req.getParameter("telefone"));
                    ps.setString(6, req.getParameter("email"));
                    ps.setString(7, req.getParameter("endereco"));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/clientes");
                return;
            }
            if (path.equals("/admin/clientes/excluir")) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("DELETE FROM tutor WHERE cpf=?")) {
                    ps.setString(1, req.getParameter("cpf"));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/clientes");
                return;
            }
            if (path.equals("/admin/avaliacoes/salvar")) {
                if (!admin(req, resp))
                    return;
                int nota = Integer.parseInt(req.getParameter("nota"));
                if (nota < 1 || nota > 3) {
                    resp.sendError(400, "Nota deve ser de 1 a 3.");
                    return;
                }
                String id = req.getParameter("id"), atendimento = req.getParameter("atendimento"),
                        comentario = req.getParameter("comentario");
                if (id == null || id.isBlank()) {
                    try (Connection c = MySql.connect();
                            PreparedStatement ps = c.prepareStatement(
                                    "INSERT INTO avaliacao(nota,comentario,id_atendimentoFK) VALUES(?,?,?)")) {
                        ps.setInt(1, nota);
                        ps.setString(2, comentario);
                        if (atendimento == null || atendimento.isBlank())
                            ps.setNull(3, java.sql.Types.INTEGER);
                        else
                            ps.setInt(3, Integer.parseInt(atendimento));
                        ps.executeUpdate();
                    }
                } else
                    try (Connection c = MySql.connect();
                            PreparedStatement ps = c.prepareStatement(
                                    "UPDATE avaliacao SET nota=?,comentario=? WHERE id_avaliacao=?")) {
                        ps.setInt(1, nota);
                        ps.setString(2, comentario);
                        ps.setInt(3, Integer.parseInt(id));
                        ps.executeUpdate();
                    }
                resp.sendRedirect(req.getContextPath() + "/avaliacoes");
                return;
            }
            if (path.equals("/admin/avaliacoes/excluir")) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("DELETE FROM avaliacao WHERE id_avaliacao=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("id")));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/avaliacoes");
                return;
            }
            if (path.equals("/admin/produtos/salvar")) {
                if (!admin(req, resp))
                    return;
                String id = req.getParameter("id");
                if (id == null || id.isBlank())
                    try (Connection c = MySql.connect();
                            PreparedStatement ps = c.prepareStatement(
                                    "INSERT INTO produto(nome,marca,tipo,estoque,preco,url) VALUES(?,?,?,?,?,?)")) {
                        ps.setString(1, req.getParameter("nome"));
                        ps.setString(2, req.getParameter("marca"));
                        ps.setString(3, req.getParameter("categoria"));
                        ps.setInt(4, Integer.parseInt(req.getParameter("estoque")));
                        ps.setBigDecimal(5, new java.math.BigDecimal(req.getParameter("preco")));
                        ps.setString(6, req.getParameter("url"));
                        ps.executeUpdate();
                    }
                else
                    try (Connection c = MySql.connect();
                            PreparedStatement ps = c.prepareStatement(
                                    "UPDATE produto SET nome=?,marca=?,tipo=?,estoque=?,preco=?,url=? WHERE id_produto=?")) {
                        ps.setString(1, req.getParameter("nome"));
                        ps.setString(2, req.getParameter("marca"));
                        ps.setString(3, req.getParameter("categoria"));
                        ps.setInt(4, Integer.parseInt(req.getParameter("estoque")));
                        ps.setBigDecimal(5, new java.math.BigDecimal(req.getParameter("preco")));
                        ps.setString(6, req.getParameter("url"));
                        ps.setInt(7, Integer.parseInt(id));
                        ps.executeUpdate();
                    }
                resp.sendRedirect(req.getContextPath() + "/loja");
                return;
            }
            if (path.equals("/admin/produtos/excluir")) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement("DELETE FROM produto WHERE id_produto=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("id")));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/loja");
                return;
            }
            if (path.equals("/admin/fila/salvar")) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO fila_atendimento(criticidade,data_hora_entrada,status,id_servicoFK,id_petFK) VALUES(?,CURRENT_TIMESTAMP,'Em espera',?,?)")) {
                    ps.setString(1, req.getParameter("servicoNome"));
                    ps.setInt(2, Integer.parseInt(req.getParameter("servico")));
                    ps.setInt(3, Integer.parseInt(req.getParameter("pet")));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/fila");
                return;
            }
            if (path.equals("/admin/fila/excluir")) {
                if (!admin(req, resp))
                    return;
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c
                                .prepareStatement("DELETE FROM fila_atendimento WHERE id_atendimento=?")) {
                    ps.setInt(1, Integer.parseInt(req.getParameter("id")));
                    ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath() + "/fila");
                return;
            }
            if ("/login/usuario".equals(path) || "/api/login".equals(path)) {
                Map<String, Object> b = body(req);
                String usuario = value(b, "usuario"), senha = String.valueOf(b.getOrDefault("senha", ""));
                if (usuario.isBlank() || senha.isBlank()) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Informe usuário e senha."));
                    return;
                }
                List<Map<String, Object>> found = query(
                        "SELECT cpf,nome,usuario,senha FROM tutor WHERE usuario=? LIMIT 1", usuario);
                if (!found.isEmpty() && String.valueOf(found.get(0).get("senha")).equals(senha)) {
                    Map<String, Object> u = found.get(0);
                    List<Map<String, Object>> conta = query("SELECT status_conta FROM tutor WHERE cpf=? LIMIT 1",
                            u.get("cpf"));
                    String status = conta.isEmpty() ? null : String.valueOf(conta.get(0).get("status_conta"));
                    if (status != null && !status.isBlank() && !"Ativo".equalsIgnoreCase(status)) {
                        json(resp, 403,
                                Map.of("sucesso", false, "mensagem", "Esta conta está inativa. Procure o pet shop."));
                        return;
                    }
                    u.remove("senha");
                    json(resp, 200, Map.of("sucesso", true, "cpf", u.get("cpf"), "nome", u.get("nome"), "usuario",
                            u.get("usuario")));
                } else
                    json(resp, 401, Map.of("sucesso", false, "mensagem", "Usuário ou senha incorretos."));
                return;
            }
            if ("/api/vendas".equals(path)) {
                Map<String, Object> b = body(req);
                String cpf = Validacao.somenteDigitos(value(b, "cpf"));
                Integer produtoId = Validacao.inteiroOuNull(value(b, "id_produto"));
                Integer quantidade = Validacao.inteiroOuNull(value(b, "quantidade"));
                String pagamento = value(b, "forma_pagamento");
                if (cpf.isBlank() || produtoId == null || quantidade == null || quantidade < 1) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Informe tutor, produto e quantidade válida."));
                    return;
                }
                if (query("SELECT cpf FROM tutor WHERE " + cpfSemMascaraSql("cpf") + "=? LIMIT 1", cpf).isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Tutor não encontrado para este CPF."));
                    return;
                }
                try (Connection c = MySql.connect()) {
                    c.setAutoCommit(false);
                    try {
                        BigDecimal preco;
                        Integer estoque;
                        try (PreparedStatement p = c.prepareStatement(
                                "SELECT preco,estoque FROM produto WHERE id_produto=? FOR UPDATE")) {
                            p.setInt(1, produtoId);
                            try (ResultSet rs = p.executeQuery()) {
                                if (!rs.next()) {
                                    c.rollback();
                                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Produto não encontrado."));
                                    return;
                                }
                                preco = rs.getBigDecimal("preco");
                                Object saldo = rs.getObject("estoque");
                                estoque = saldo == null ? null : ((Number) saldo).intValue();
                            }
                        }
                        if (preco == null || (estoque != null && estoque < quantidade)) {
                            c.rollback();
                            json(resp, 409, Map.of("sucesso", false, "mensagem", "Estoque insuficiente para este produto."));
                            return;
                        }
                        BigDecimal subtotal = preco.multiply(BigDecimal.valueOf(quantidade));
                        int vendaId;
                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO venda(forma_pagamento,subtotal,status) VALUES(?,?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            p.setString(1, pagamento.isBlank() ? "Pix" : Validacao.truncar(pagamento, 50));
                            p.setBigDecimal(2, subtotal);
                            p.setString(3, "Concluída");
                            p.executeUpdate();
                            try (ResultSet keys = p.getGeneratedKeys()) {
                                if (!keys.next()) throw new SQLException("Venda sem identificador gerado.");
                                vendaId = keys.getInt(1);
                            }
                        }
                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO item_venda(id_produtoFK,id_vendaFK,preco_unitario,subtotal,quantidade) VALUES(?,?,?,?,?)")) {
                            p.setInt(1, produtoId);
                            p.setInt(2, vendaId);
                            p.setBigDecimal(3, preco);
                            p.setBigDecimal(4, subtotal);
                            p.setInt(5, quantidade);
                            p.executeUpdate();
                        }
                        try (PreparedStatement p = c.prepareStatement(
                                "UPDATE produto SET estoque=estoque-? WHERE id_produto=? AND estoque IS NOT NULL AND estoque>=?")) {
                            p.setInt(1, quantidade);
                            p.setInt(2, produtoId);
                            p.setInt(3, quantidade);
                            int atualizados = p.executeUpdate();
                            if (estoque != null && atualizados == 0) {
                                c.rollback();
                                json(resp, 409, Map.of("sucesso", false, "mensagem", "Estoque insuficiente para este produto."));
                                return;
                            }
                        }
                        c.commit();
                        json(resp, 201, Map.of("sucesso", true, "id_venda", vendaId, "total", subtotal,
                                "mensagem", "Compra realizada."));
                    } catch (SQLException | RuntimeException e) {
                        c.rollback();
                        throw e;
                    }
                }
                return;
            }
            if ("/api/agendamentos".equals(path)) {
                Map<String, Object> b = body(req);
                String cpf = Validacao.somenteDigitos(value(b, "cpf"));
                Integer petId = Validacao.inteiroOuNull(value(b, "idPet"));
                Integer servicoId = Validacao.inteiroOuNull(value(b, "idServico"));
                String data = value(b, "data"), hora = value(b, "hora");
                if (cpf.isBlank() || petId == null || servicoId == null || data.isBlank() || hora.isBlank()) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Informe tutor, pet, serviço, data e hora."));
                    return;
                }
                List<Map<String, Object>> tutores = query("SELECT cpf FROM tutor WHERE " + cpfSemMascaraSql("cpf") + "=? LIMIT 1", cpf);
                if (tutores.isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Tutor não encontrado para este CPF."));
                    return;
                }
                List<Map<String, Object>> pets = query("SELECT codigo_pet FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE " + cpfSemMascaraSql("t.cpf") + "=? AND p.codigo_pet=? LIMIT 1", cpf, petId);
                if (pets.isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Pet não encontrado para este tutor."));
                    return;
                }
                String cpfTutor = String.valueOf(tutores.get(0).get("cpf"));
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO agenda(codigo_servico,cpf,data_agendamento,hora_agendamento,codigo_pet,status) VALUES(?,?,STR_TO_DATE(?,'%Y-%m-%d'),STR_TO_DATE(?,'%H:%i'),?,'Agendado')",
                                Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, servicoId);
                    ps.setString(2, cpfTutor);
                    ps.setString(3, data);
                    ps.setString(4, hora);
                    ps.setInt(5, petId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        json(resp, 201, Map.of("sucesso", true, "id", keys.getInt(1), "status", "Agendado"));
                    }
                }
                return;
            }
            if ("/api/fila/adicionar".equals(path)) {
                Map<String, Object> b = body(req);
                String cpf = Validacao.somenteDigitos(value(b, "cpf")), nomePet = value(b, "pet"),
                        nomeServico = value(b, "servico");
                if (cpf.isBlank() || nomePet.isBlank() || nomeServico.isBlank()) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Informe CPF, pet e serviço."));
                    return;
                }
                Integer idPetInformado = Validacao.inteiroOuNull(value(b, "idPet"));
                List<Map<String, Object>> pets = idPetInformado != null
                        ? query("SELECT p.codigo_pet FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE " + cpfSemMascaraSql("t.cpf") + "=? AND p.codigo_pet=? LIMIT 1",
                                cpf, idPetInformado)
                        : query("SELECT p.codigo_pet FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE " + cpfSemMascaraSql("t.cpf") + "=? AND p.nome=? LIMIT 1",
                                cpf, nomePet);
                List<Map<String, Object>> servicos = query("SELECT codigo_servico FROM servico WHERE nome=? LIMIT 1",
                        nomeServico);
                if (pets.isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem",
                            "Pet \"" + nomePet + "\" não encontrado no cadastro deste tutor."));
                    return;
                }
                if (servicos.isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem",
                            "Serviço \"" + nomeServico + "\" não encontrado no cadastro."));
                    return;
                }
                int petId = ((Number) pets.get(0).get("codigo_pet")).intValue();
                int servicoId = ((Number) servicos.get(0).get("codigo_servico")).intValue();
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO fila_atendimento (criticidade,data_hora_entrada,status,id_servicoFK,id_petFK) VALUES (?,CURRENT_TIMESTAMP,'Em espera',?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nomeServico);
                    ps.setInt(2, servicoId);
                    ps.setInt(3, petId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        int id = keys.getInt(1);
                        List<Map<String, Object>> pos = query(
                                "SELECT COUNT(*) AS total FROM fila_atendimento WHERE status='Em espera' AND id_atendimento<=?",
                                id);
                        json(resp, 201, Map.of("id", id, "sucesso", true, "status", "Em espera", "posicao",
                                pos.get(0).get("total"), "esperaMinutos", 0));
                    }
                }
                return;
            }
            if (path != null && path.matches("/api/fila/\\d+/cancelar")) {
                String cpf = req.getParameter("cpf");
                int id = Integer.parseInt(path.split("/")[3]);
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "UPDATE fila_atendimento f JOIN pet p ON p.codigo_pet=f.id_petFK JOIN tutela t ON t.codigo_pet=p.codigo_pet SET f.status='Cancelado' WHERE f.id_atendimento=? AND t.cpf=? AND f.status='Em espera'")) {
                    ps.setInt(1, id);
                    ps.setString(2, Validacao.somenteDigitos(cpf));
                    if (ps.executeUpdate() == 0) {
                        json(resp, 404,
                                Map.of("sucesso", false, "mensagem", "Atendimento não encontrado ou já iniciado."));
                        return;
                    }
                }
                json(resp, 200, Map.of("sucesso", true));
                return;
            }
            if ("/cadastrar/pet".equals(path)) {
                Map<String, Object> b = body(req);
                String cpf = Validacao.somenteDigitos(value(b, "cpf")), nome = value(b, "nome"),
                        especie = value(b, "especie");
                Integer idade = Validacao.inteiroOuNull(value(b, "idade"));
                if (cpf.isBlank() || nome.isBlank()) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF e nome do pet são obrigatórios."));
                    return;
                }
                if (!Validacao.cpfValido(cpf)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF inválido: informe os 11 dígitos."));
                    return;
                }
                List<Map<String, Object>> tutores = query(
                        "SELECT cpf FROM tutor WHERE " + cpfSemMascaraSql("cpf") + "=? LIMIT 1", cpf);
                if (tutores.isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Tutor não encontrado para este CPF."));
                    return;
                }
                String cpfTutor = String.valueOf(tutores.get(0).get("cpf"));
                String raca = value(b, "raca"), porte = value(b, "porte"), documento = value(b, "n_documento");
                BigDecimal peso = null;
                String pesoTexto = value(b, "peso").replace(",", ".");
                if (!pesoTexto.isBlank()) {
                    try {
                        peso = new BigDecimal(pesoTexto);
                    } catch (NumberFormatException ignored) {
                    }
                }
                try (Connection c = MySql.connect()) {
                    c.setAutoCommit(false);
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO pet(nome,especie,idade,raca,porte,peso,n_documento) VALUES(?,?,?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        p.setString(1, Validacao.truncar(nome, 100));
                        p.setString(2, Validacao.truncar(especie, 50));
                        if (idade == null)
                            p.setNull(3, java.sql.Types.INTEGER);
                        else
                            p.setInt(3, idade);
                        p.setString(4, Validacao.truncar(raca, 50));
                        p.setString(5, Validacao.truncar(porte, 50));
                        if (peso == null)
                            p.setNull(6, java.sql.Types.DECIMAL);
                        else
                            p.setBigDecimal(6, peso);
                        if (documento.isBlank())
                            p.setNull(7, java.sql.Types.VARCHAR);
                        else
                            p.setString(7, Validacao.truncar(documento, 20));
                        p.executeUpdate();
                        try (ResultSet k = p.getGeneratedKeys()) {
                            k.next();
                            try (PreparedStatement t = c
                                    .prepareStatement("INSERT INTO tutela(codigo_pet,cpf) VALUES(?,?)")) {
                                t.setInt(1, k.getInt(1));
                                t.setString(2, cpfTutor);
                                t.executeUpdate();
                            }
                        }
                    }
                    c.commit();
                }
                json(resp, 201, Map.of("sucesso", true, "mensagem", "Pet cadastrado."));
                return;
            }
            if ("/api/avaliacoes".equals(path)) {
                Map<String, Object> b = body(req);
                Integer nota = Validacao.inteiroOuNull(String.valueOf(b.getOrDefault("nota", "")));
                if (nota == null || nota < 1 || nota > 3) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "A nota deve ser 1, 2 ou 3."));
                    return;
                }
                Object id = b.getOrDefault("id_atendimento", b.get("servico_id"));
                String cpf = Validacao.somenteDigitos(value(b, "cpf"));
                if (id == null || cpf.isBlank() || query(
                        "SELECT f.id_atendimento FROM fila_atendimento f JOIN pet p ON p.codigo_pet=f.id_petFK JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE f.id_atendimento=? AND " + cpfSemMascaraSql("t.cpf") + "=? AND f.status IN ('Concluído','Concluido','Finalizado') LIMIT 1",
                        Integer.parseInt(id.toString()), cpf).isEmpty()) {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Atendimento concluído não encontrado para este tutor."));
                    return;
                }
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO avaliacao (nota,comentario,id_atendimentoFK) VALUES (?,?,?)")) {
                    ps.setInt(1, nota);
                    ps.setString(2, Validacao.truncar(String.valueOf(b.getOrDefault("comentario", "")), 255));
                    ps.setInt(3, Integer.parseInt(id.toString()));
                    ps.executeUpdate();
                }
                json(resp, 201, Map.of("sucesso", true, "mensagem", "Avaliação enviada."));
                return;
            }
            json(resp, 404, Map.of("sucesso", false, "mensagem", "Rota não encontrada."));
        } catch (SQLException | NumberFormatException e) {
            json(resp, 400, Map.of("sucesso", false, "mensagem", "Dados inválidos ou falha na operação MySQL."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null || path.isEmpty())
            path = req.getServletPath();
        if (path != null && path.matches("/api/agendamentos/\\d+/cancelar")) {
            try {
                int id = Integer.parseInt(path.split("/")[3]);
                String cpf = Validacao.somenteDigitos(req.getParameter("cpf"));
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "UPDATE agenda SET status='Cancelado' WHERE id_agenda=? AND " + cpfSemMascaraSql("cpf") + "=? AND status='Agendado'")) {
                    ps.setInt(1, id);
                    ps.setString(2, cpf);
                    if (ps.executeUpdate() == 0) {
                        json(resp, 404, Map.of("sucesso", false, "mensagem", "Agendamento não encontrado para este tutor."));
                        return;
                    }
                }
                json(resp, 200, Map.of("sucesso", true));
            } catch (SQLException | NumberFormatException e) {
                json(resp, 400, Map.of("sucesso", false, "mensagem", "Não foi possível cancelar o agendamento."));
            }
        } else if (path != null && path.matches("/api/pets/\\d+"))
            try {
                Map<String, Object> b = body(req);
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                String cpf = Validacao.somenteDigitos(value(b, "cpf"));
                if (!Validacao.cpfValido(cpf)) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF inválido: informe os 11 dígitos."));
                    return;
                }
                BigDecimal peso = Validacao.decimalOuNull(value(b, "peso"));
                Integer idade = Validacao.inteiroOuNull(value(b, "idade"));
                if (!value(b, "peso").isEmpty() && peso == null) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Peso inválido. Use por exemplo 12.5"));
                    return;
                }
                if (!value(b, "idade").isEmpty() && idade == null) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "Idade inválida. Informe um número inteiro."));
                    return;
                }
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "UPDATE pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet SET p.peso=?,p.n_documento=?,p.idade=? WHERE p.codigo_pet=? AND t.cpf=?")) {
                    if (peso == null)
                        ps.setNull(1, java.sql.Types.DECIMAL);
                    else
                        ps.setBigDecimal(1, peso);
                    ps.setString(2, Validacao.truncar(value(b, "n_documento"), 20));
                    if (idade == null)
                        ps.setNull(3, java.sql.Types.INTEGER);
                    else
                        ps.setInt(3, idade);
                    ps.setInt(4, id);
                    ps.setString(5, cpf);
                    if (ps.executeUpdate() == 0) {
                        json(resp, 404, Map.of("sucesso", false, "mensagem", "Pet não encontrado para este tutor."));
                        return;
                    }
                }
                json(resp, 200, Map.of("sucesso", true));
            } catch (SQLException | NumberFormatException e) {
                json(resp, 400, Map.of("sucesso", false, "mensagem", "Não foi possível atualizar o pet."));
            }
        else
            json(resp, 404, Map.of("sucesso", false));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty())
            path = req.getServletPath();
        if (path != null && path.matches("/api/pets/\\d+"))
            try {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                String cpf = req.getParameter("cpf");
                try (Connection c = MySql.connect();
                        PreparedStatement ps = c.prepareStatement(
                                "DELETE p FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE p.codigo_pet=? AND " + cpfSemMascaraSql("t.cpf") + "=?")) {
                    ps.setInt(1, id);
                    ps.setString(2, cpf);
                    if (ps.executeUpdate() == 0) {
                        json(resp, 404, Map.of("sucesso", false, "mensagem", "Pet não encontrado."));
                        return;
                    }
                }
                json(resp, 200, Map.of("sucesso", true));
            } catch (SQLException e) {
                json(resp, 400, Map.of("sucesso", false));
            }
        else
            json(resp, 404, Map.of("sucesso", false));
    }

    /** Soma a coluna valor (DECIMAL) das linhas de receita/despesa. */
    private static BigDecimal somar(List<Map<String, Object>> linhas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> linha : linhas) {
            Object v = linha.get("valor");
            if (v instanceof BigDecimal b) {
                total = total.add(b);
            } else if (v != null) {
                total = total.add(new BigDecimal(String.valueOf(v)));
            }
        }
        return total;
    }

    /** Fila com os campos que as telas de balcão e veterinário usam. */
    private static List<Map<String, Object>> filaCompleta() throws SQLException {
        return query("SELECT f.id_atendimento AS id, COALESCE(s.nome, f.criticidade) AS servico, f.status, f.data_hora_entrada AS horarioChegada, f.metodo_cancelamento AS motivoCancelamento, p.nome AS nomePet, t.nome AS nomeCliente, CASE WHEN f.status IN ('Em espera','Em atendimento') THEN TIMESTAMPDIFF(MINUTE,f.data_hora_entrada,CURRENT_TIMESTAMP) ELSE NULL END AS minutosEspera, CASE WHEN f.status='Em atendimento' THEN 'badge-atendimento' WHEN f.status IN ('Concluído','Concluido','Finalizado') THEN 'badge-ativo' ELSE 'badge-espera' END AS statusClass FROM fila_atendimento f LEFT JOIN servico s ON s.codigo_servico=f.id_servicoFK LEFT JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN tutor t ON t.cpf=tu.cpf ORDER BY f.data_hora_entrada");
    }

    /** Move mensagem/erro da sessão para o request (flash sem framework). */
    private static void flash(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null)
            return;
        for (String k : new String[] { "mensagem", "erro" }) {
            Object v = s.getAttribute(k);
            if (v != null) {
                req.setAttribute(k, v);
                s.removeAttribute(k);
            }
        }
    }

    /** Página para onde voltar depois de uma ação da fila. */
    private static String voltar(HttpServletRequest req) {
        String v = req.getParameter("voltar");
        if ("balconista".equals(v))
            return "/balconista";
        if ("veterinario".equals(v))
            return "/veterinario";
        return "/fila";
    }

    private static List<Map<String, Object>> query(String sql, Object... args) throws SQLException {
        try (Connection c = MySql.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++)
                ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= n; i++) {
                        Object v = rs.getObject(i);
                        row.put(rs.getMetaData().getColumnLabel(i),
                                v instanceof Timestamp t ? t.toLocalDateTime().toString() : v);
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    private static Map<String, Object> body(HttpServletRequest req) throws IOException {
        if (req.getContentType() != null && req.getContentType().toLowerCase().contains("application/json"))
            return JSON.readValue(req.getInputStream(), new TypeReference<>() {
            });
        Map<String, Object> values = new LinkedHashMap<>();
        req.getParameterMap().forEach((key, items) -> {
            if (items != null && items.length > 0)
                values.put(key, items[0]);
        });
        return values;
    }

    private static String value(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static void json(HttpServletResponse resp, int status, Object value) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        JSON.writeValue(resp.getOutputStream(), value);
    }

    private static boolean admin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getSession(false) == null || req.getSession(false).getAttribute("adminLogado") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}

