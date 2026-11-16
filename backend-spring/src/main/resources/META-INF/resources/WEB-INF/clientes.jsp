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
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila</a>
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
        <h2>Gerenciamento de Clientes / Usuários</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Clientes cadastrados tanto pelo Aplicativo Mobile quanto pela Web.</p>

        <c:if test="${not empty mensagem}">
            <div style="background:#e8f5e9; border:1px solid #a5d6a7; color:#2e7d32; padding:1rem 1.2rem; border-radius:8px; margin-bottom:1.4rem; font-weight:600;">
                ✅ ${mensagem}
            </div>
        </c:if>
        <c:if test="${not empty erro}">
            <div style="background:#ffebee; border:1px solid #ef9a9a; color:#c62828; padding:1rem 1.2rem; border-radius:8px; margin-bottom:1.4rem; font-weight:600;">
                ⚠️ ${erro}
            </div>
        </c:if>

        <!-- RF01 — Consultar cliente por CPF ou nome -->
        <form action="/clientes" method="GET" style="display:flex; gap:0.6rem; margin-bottom:1.2rem;">
            <input type="text" name="busca" value="${busca}" placeholder="Buscar por CPF ou nome do cliente..."
                   style="flex:1; padding:0.7rem; border:1px solid #ccc; border-radius:6px;">
            <button type="submit" class="btn btn-sucesso" style="padding:0.7rem 1.4rem;">Buscar</button>
            <a href="/clientes" class="btn" style="padding:0.7rem 1.2rem; background:#eee; color:#333; text-decoration:none; border-radius:6px;">Limpar</a>
        </form>

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
                    <th>Status</th>
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
                            <c:choose>
                                <c:when test="${c.bloqueado}">
                                    <span class="badge badge-cancelado">🚫 ${c.statusConta}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-concluido">✅ ${c.statusConta}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.bloqueado}">
                                    <a href="/admin/clientes/editar?cpf=${c.cpf}"
                                       class="btn btn-sucesso btn-sm"
                                       style="text-decoration:none; display:inline-block; margin-right:0.3rem;">Editar</a>
                                    <form action="/admin/clientes/desbloquear" method="POST" style="display:inline;">
                                        <input type="hidden" name="cpf" value="${c.cpf}">
                                        <button type="submit" class="btn btn-sucesso btn-sm">Desbloquear</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <a href="/admin/clientes/editar?cpf=${c.cpf}"
                                       class="btn btn-sucesso btn-sm"
                                       style="text-decoration:none; display:inline-block; margin-right:0.3rem;">Editar</a>
                                    <form action="/admin/clientes/bloquear" method="POST" style="display:inline;">
                                        <input type="hidden" name="cpf" value="${c.cpf}">
                                        <input type="text" name="motivo" required placeholder="Motivo do bloqueio"
                                               style="width:150px; padding:4px 6px; font-size:0.8rem; border:1px solid #ccc; border-radius:4px;">
                                        <button type="submit" class="btn btn-cancelar btn-sm"
                                                onclick="return confirm('Bloquear este cliente?')">Bloquear</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                            <form action="/admin/clientes/excluir" method="POST" style="display:inline;">
                                <input type="hidden" name="cpf" value="${c.cpf}">
                                <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja realmente excluir este cliente?')">Excluir</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty clientes}">
                    <tr>
                        <td colspan="8" style="text-align: center; color: #888; padding: 2rem;">Nenhum cliente cadastrado ainda.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </main>
</body>
</html>
