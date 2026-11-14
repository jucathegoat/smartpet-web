package com.petshop.backend.servlet;

import java.math.BigDecimal;

/**
 * Validação e normalização dos dados que chegam do app Flutter.
 *
 * Só valida o que o app envia — não altera tabela, coluna nem tipo no banco.
 * O objetivo é trocar o 500/"Dados inválidos" genérico por uma mensagem clara,
 * e evitar que um número malformado derrube a requisição.
 */
final class Validacao {
    private Validacao() { }

    /** CPF: mantém só dígitos (o app pode mandar "123.456.789-00"). */
    static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    static boolean cpfValido(String cpf) {
        String digitos = somenteDigitos(cpf);
        return digitos.length() == 11;
    }

    static boolean usuarioValido(String usuario) {
        if (usuario == null) return false;
        String u = usuario.trim();
        return u.length() >= 3 && u.length() <= 50 && u.indexOf(' ') < 0;
    }

    static boolean senhaValida(String senha) {
        return senha != null && senha.length() >= 4 && senha.length() <= 50;
    }

    static boolean emailValido(String email) {
        if (email == null) return false;
        String e = email.trim();
        int arroba = e.indexOf('@');
        return arroba > 0 && arroba < e.length() - 1 && e.indexOf(' ', arroba) < 0;
    }

    /** Número decimal opcional; devolve null quando vazio ou inválido. */
    static BigDecimal decimalOuNull(String valor) {
        if (valor == null) return null;
        String v = valor.trim().replace(',', '.');
        if (v.isEmpty()) return null;
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Inteiro opcional; devolve null quando vazio ou inválido. */
    static Integer inteiroOuNull(String valor) {
        if (valor == null) return null;
        String v = valor.trim();
        if (v.isEmpty()) return null;
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String truncar(String valor, int maximo) {
        if (valor == null) return "";
        String v = valor.trim();
        return v.length() <= maximo ? v : v.substring(0, maximo);
    }
}
