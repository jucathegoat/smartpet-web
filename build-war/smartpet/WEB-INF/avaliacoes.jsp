<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Avaliações - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <header class="navbar">
                <div class="logo">🐾 SmartPet Admin</div>
                <nav><a href="${pageContext.request.contextPath}/fila">Fila</a><a
                        href="${pageContext.request.contextPath}/loja">Loja</a><a
                        href="${pageContext.request.contextPath}/pets">Pets</a><a
                        href="${pageContext.request.contextPath}/clientes">Clientes</a><a class="active"
                        href="${pageContext.request.contextPath}/avaliacoes">Avaliações</a><a
                        href="${pageContext.request.contextPath}/logout">Sair</a></nav>
            </header>
            <main class="container">
                <h2>Avaliações dos clientes</h2>
                <form action="${pageContext.request.contextPath}/admin/avaliacoes/salvar" method="post" class="card" style="padding:18px;margin-bottom:18px;display:flex;gap:10px;flex-wrap:wrap"><input type="number" name="id" placeholder="ID para editar (vazio = nova)" min="1"><input type="number" name="atendimento" placeholder="Atendimento opcional" min="1"><select name="nota"><option value="3">3 · Excelente</option><option value="2">2 · Bom</option><option value="1">1 · Ruim</option></select><input name="comentario" maxlength="255" placeholder="Comentário" required style="flex:1;min-width:220px"><button class="btn btn-sucesso">Salvar avaliação</button></form>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Pet</th>
                            <th>Serviço</th>
                            <th>Nota</th>
                            <th>Comentário</th>
                            <th>Atendimento</th><th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${avaliacoes}">
                            <tr>
                                <td>#
                                    <c:out value="${a.id}" />
                                </td>
                                <td>
                                    <c:out value="${a.nomePet}" />
                                </td>
                                <td>
                                    <c:out value="${a.servico}" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.nota == 3}">⭐⭐⭐ Excelente</c:when>
                                        <c:when test="${a.nota == 2}">⭐⭐ Bom</c:when>
                                        <c:when test="${a.nota == 1}">⭐ Ruim</c:when>
                                        <c:otherwise>
                                            <c:out value="${a.nota}" />
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:out value="${a.comentario}" />
                                </td>
                                <td>
                                    <c:out value="${a.atendimento}" />
                                </td>
                                <td><form action="${pageContext.request.contextPath}/admin/avaliacoes/excluir" method="post"><input type="hidden" name="id" value="${a.id}"><button class="btn btn-perigo btn-sm" onclick="return confirm('Excluir avaliação?')">Excluir</button></form></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty avaliacoes}">
                            <tr>
                                <td colspan="7" style="text-align:center;padding:2rem">Nenhuma avaliação registrada.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </main>
        </body>

        </html>
