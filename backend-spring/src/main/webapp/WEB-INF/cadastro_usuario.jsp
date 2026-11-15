<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Novo Usuário - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .form-card {
            background: #fff;
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            max-width: 600px;
            margin: 0 auto;
        }
        .form-group { margin-bottom: 1.2rem; }
        .form-group label { display: block; margin-bottom: 0.4rem; font-weight: 600; }
        .form-group input { width: 100%; padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
        .alert-erro { background: #ffebee; color: #c62828; padding: 0.8rem; border-radius: 6px; margin-bottom: 1.2rem; }
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
            <h2>Cadastrar Novo Usuário / Cliente</h2>
            <p style="color: #666; margin-bottom: 1.5rem;">Cria uma conta que funciona tanto no painel Web quanto no aplicativo Flutter.</p>

            <c:if test="">
                <div class="alert-erro"></div>
            </c:if>

            <form action="/admin/usuarios/novo" method="POST">
                <div class="form-group">
                    <label>Nome Completo</label>
                    <input type="text" name="nome" required placeholder="Ex: Maria Santos">
                </div>
                <div class="form-group">
                    <label>CPF (apenas números)</label>
                    <input type="text" name="cpf" required placeholder="Ex: 11122233344">
                </div>
                <div class="form-group">
                    <label>Telefone / WhatsApp</label>
                    <input type="text" name="telefone" placeholder="Ex: (11) 98765-4321">
                </div>
                <div class="form-group">
                    <label>E-mail</label>
                    <input type="email" name="email" placeholder="Ex: maria@email.com">
                </div>
                <div class="form-group">
                    <label>Endereço</label>
                    <input type="text" name="endereco" placeholder="Ex: Rua das Palmeiras, 50">
                </div>
                <div class="form-group">
                    <label>Nome de Usuário (Login)</label>
                    <input type="text" name="usuario" required placeholder="Ex: mariasantos">
                </div>
                <div class="form-group">
                    <label>Senha de Acesso</label>
                    <input type="password" name="senha" required placeholder="Digite a senha">
                </div>
                <div style="display: flex; gap: 10px; margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-sucesso" style="flex: 1; padding: 0.8rem;">Salvar Cadastro</button>
                    <a href="/clientes" class="btn" style="background:#e0e0e0; color:#333; text-decoration:none; padding:0.8rem 1.5rem; border-radius:6px; font-weight:600;">Voltar</a>
                </div>
            </form>
        </div>
    </main>
</body>
</html>