package com.petshop.backend.servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Abre conexões JDBC usando somente configuração externa ao código. */
public final class MySql {
    private MySql() {}

    public static Connection connect() throws SQLException {
        String url = setting("SMARTPET_DB_URL", "jdbc:mysql://localhost:3306/smartpet?serverTimezone=UTC");
        String user = setting("SMARTPET_DB_USER", "root");
        String password = setting("SMARTPET_DB_PASSWORD", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is missing from the application.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }
}
