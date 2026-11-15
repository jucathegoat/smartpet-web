<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Clientes / Tutores - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
        <nav>
            <a href="/fila">Fila</a>
            <a href="/pets">Pets</a>
            <a href="/clientes">Clientes</a>
            <a href="/loja">Loja</a>
            <a href="/caixa">Caixa</a>
            <a href="/avaliacoes">Avaliações</a>
            <a href="/notificacoes">Notificações</a>
            <a href="/logout" style="color: #ffcdd2;">Sair</a>
        </nav>
    </header>
    <main class="container">
        <h2>Gerenciamento de Clientes / Usuários</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Clientes cadastrados tanto pelo Aplicativo Mobile quanto pela Web.</p>

        <div style="margin-bottom: 1.5rem;">
            <a href="/admin/usuarios/novo" class="btn btn-sucesso" style="text-decoration: none; padding: 10px 18px; border-radius: 6px; font-weight: 600;">➕ Cadastrar Novo Cliente / Usuário</a>
        </div>

        <table class="tabela">
            <thead>
                <tr>
                    <th>CPF</th>
                    <th>Nome</th>
                    <th>Usuário (Login)</th>
                    <th>Telefone</th>
                    <th>Email</th>
                    <th>Endereço</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${clientes}">
                    <tr>
                        <td><strong>${c.cpf}</strong></td>
                        <td>${c.nome}</td>
                        <td><span class="badge" style="background:#e8f5e9; color:#2e7d32;">${c.usuario}</span></td>
                        <td>${c.telefone}</td>
                        <td>${c.email}</td>
                        <td>${c.endereco}</td>
                        <td>
                            <form action="/admin/clientes/excluir" method="POST" style="display:inline;">
                                <input type="hidden" name="cpf" value="${c.cpf}">
                                <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja realmente excluir este cliente?')">Excluir</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty clientes}">
                    <tr>
                        <td colspan="7" style="text-align: center; color: #888; padding: 2rem;">Nenhum cliente cadastrado ainda.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </main>
</body>
</html>