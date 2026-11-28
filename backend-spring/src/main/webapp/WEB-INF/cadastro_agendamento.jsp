<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Novo Agendamento - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .form-card {
            background: #fff;
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            max-width: 500px;
            margin: 0 auto;
        }
        .form-group { margin-bottom: 1.2rem; }
        .form-group label { display: block; margin-bottom: 0.4rem; font-weight: 600; }
        .form-group input, .form-group select { width: 100%; padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
        <nav>
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila</a>
            <a href="/admin/agendamentos">Agendamentos</a>
            <a href="/pets">Pets</a>
            <a href="/clientes">Clientes</a>
            <a href="/loja">Loja</a>
            <a href="/vendas">Vendas</a>
            <a href="/caixa">Caixa</a>
            <a href="/avaliacoes">Avaliações</a>
            <a href="/notificacoes">Notificações</a>
            <a href="/logout" style="color: #ffcdd2;">Sair</a>
        </nav>
    </header>
    <main class="container">
        <div class="form-card">
            <h2>➕ Novo Agendamento na Fila</h2>
            <p style="color: #666; margin-bottom: 1.5rem;">Entrada manual de pet na fila de espera do Pet Shop.</p>

            <form action="/admin/agendamentos/novo" method="POST">
                <div class="form-group">
                    <label>Pet cadastrado (lista do banco)</label>
                    <select name="idPet" required>
                        <option value="">Selecione o pet...</option>
                        <c:forEach var="p" items="${pets}">
                            <option value="${p.id}">${p.nome} — ${p.especie}</option>
                        </c:forEach>
                    </select>
                    <small style="color:#888;">Os pets vêm da tabela <code>pet</code>. Cadastre novos em Clientes → Editar.</small>
                </div>
                <div class="form-group">
                    <label>Serviço Desejado (lista do banco)</label>
                    <select name="idServico" required>
                        <option value="">Selecione o serviço...</option>
                        <c:forEach var="s" items="${servicos}">
                            <option value="${s.idServico}">${s.nome} — R$ ${s.preco}</option>
                        </c:forEach>
                    </select>
                    <small style="color:#888;">Os serviços vêm da tabela <code>servico</code>.</small>
                </div>
                <div style="display: flex; gap: 10px; margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-sucesso" style="flex: 1; padding: 0.8rem;">Adicionar à Fila</button>
                    <a href="/fila" class="btn" style="background:#e0e0e0; color:#333; text-decoration:none; padding:0.8rem 1.5rem; border-radius:6px; font-weight:600;">Voltar</a>
                </div>
            </form>
        </div>
    </main>
</body>
</html>