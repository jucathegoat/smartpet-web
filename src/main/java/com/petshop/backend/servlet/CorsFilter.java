package com.petshop.backend.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro aplicado a todas as rotas do SmartPet.
 *
 * 1) CORS: libera o app Flutter (Android/emulador/desktop/web) para chamar a API.
 *    Sem os cabeçalhos Access-Control-Allow-*, o app Flutter Web e o emulador
 *    recebem falha de rede antes mesmo de chegar ao servlet.
 * 2) Preflight: responde OPTIONS com 204 em vez de deixar cair no servlet (404).
 * 3) Encoding: força UTF-8 em toda requisição e resposta.
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig config) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String origem = req.getHeader("Origin");
        resp.setHeader("Access-Control-Allow-Origin", origem == null || origem.isBlank() ? "*" : origem);
        resp.setHeader("Vary", "Origin");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
        resp.setHeader("Access-Control-Max-Age", "86400");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
