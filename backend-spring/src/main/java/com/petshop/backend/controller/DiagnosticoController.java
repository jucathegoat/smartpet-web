
package com.petshop.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DiagnosticoController {

    private final JdbcTemplate jdbcTemplate;

    public DiagnosticoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/diagnostico/banco")
    public Map<String, Object> testarBanco() {
        return jdbcTemplate.queryForMap(
            "SELECT DATABASE() AS banco, 1 AS conectado"
        );
    }
}
