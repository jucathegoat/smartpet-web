<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .login-wrapper {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 80vh;
        }
        .login-card {
            background: #ffffff;
            padding: 2.5rem;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
            text-align: center;
        }
        .login-card h2 {
            margin-bottom: 0.5rem;
            color: #1b5e20;
        }
        .login-card p {
            color: #666;
            margin-bottom: 1.5rem;
            font-size: 0.9rem;
        }
        .form-group {
            margin-bottom: 1.2rem;
            text-align: left;
        }
        .form-group label {
            display: block;
            margin-bottom: 0.4rem;
            font-weight: 600;
            color: #333;
            font-size: 0.9rem;
        }
        .form-group input {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 1rem;
            box-sizing: border-box;
            transition: border-color 0.2s;
        }
        .form-group input:focus {
            border-color: #1b5e20;
            outline: none;
        }
        .alert-erro {
            background-color: #ffebee;
            color: #c62828;
            padding: 0.75rem;
            border-radius: 6px;
            margin-bottom: 1.2rem;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
    </header>

    <div class="login-wrapper">
        <div class="login-card">
            <h2>Acesso Restrito</h2>
            <p>Painel Administrativo SmartPet</p>

            <c:if test="${not empty erro}">
                <div class="alert-erro">${erro}</div>
            </c:if>

            <form action="/login" method="POST">
                <div class="form-group">
                    <label for="usuario">Usuário</label>
                    <input type="text" id="usuario" name="usuario" required placeholder="Digite o usuário">
                </div>
                <div class="form-group">
                    <label for="senha">Senha</label>
                    <input type="password" id="senha" name="senha" required placeholder="Digite a senha">
                </div>
                <button type="submit" class="btn btn-sucesso" style="width: 100%; padding: 0.8rem; font-size: 1rem; margin-top: 0.5rem;">Entrar no Sistema</button>
            </form>
        </div>
    </div>
</body>
</html>