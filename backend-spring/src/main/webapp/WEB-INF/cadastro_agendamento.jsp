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
            <a href="/fila">Fila</a>
            <a href="/pets">Pets</a>
            <a href="/clientes">Clientes</a>
            <a href="/loja">Loja</a>
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
                    <label>Nome do Cliente / Tutor</label>
                    <input type="text" name="cliente" required placeholder="Ex: Lucas Ferreira">
                </div>
                <div class="form-group">
                    <label>Nome do Pet</label>
                    <input type="text" name="pet" required placeholder="Ex: Bob">
                </div>
                <div class="form-group">
                    <label>Serviço Desejado</label>
                    <select name="servico" required>
                        <option value="Banho e Tosa">Banho e Tosa</option>
                        <option value="Consulta Veterinária">Consulta Veterinária</option>
                        <option value="Vacinação">Vacinação</option>
                        <option value="Exame Geral">Exame Geral</option>
                        <option value="Hidratação e Corte">Hidratação e Corte</option>
                    </select>
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