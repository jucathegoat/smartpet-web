<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Fila - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <header class="navbar">
                <div class="logo">🐾 SmartPet Admin</div>
                <nav><a class="active" href="${pageContext.request.contextPath}/fila">Fila</a><a
                        href="${pageContext.request.contextPath}/loja">Loja</a><a
                        href="${pageContext.request.contextPath}/pets">Cadastro de pets</a><a
                        href="${pageContext.request.contextPath}/avaliacoes">Avaliações</a><a
                        href="${pageContext.request.contextPath}/logout">Sair</a></nav>
            </header>
            <main class="container">
                <h2>Fila de atendimento</h2>
                <p>Atendimentos enviados pelo aplicativo.</p>
                <form action="${pageContext.request.contextPath}/admin/fila/salvar" method="post" class="card" style="padding:18px;margin:18px 0;display:flex;gap:10px;flex-wrap:wrap"><select name="pet" required><option value="">Selecione o pet</option><c:forEach var="pet" items="${pets}"><option value="${pet.id}"><c:out value="${pet.nome}"/></option></c:forEach></select><select name="servico" required><option value="">Selecione o serviço</option><c:forEach var="s" items="${servicos}"><option value="${s.id}" data-nome="${s.nome}"><c:out value="${s.nome}"/></option></c:forEach></select><input type="hidden" name="servicoNome" id="servicoNome"><button class="btn btn-sucesso">Adicionar atendimento</button></form>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>Posição</th>
                            <th>ID</th>
                            <th>Tutor</th>
                            <th>Pet</th>
                            <th>Serviço</th>
                            <th>Chegada</th>
                            <th>Espera</th>
                            <th>Status</th><th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${fila}">
                            <tr>
                                <td>
                                    <c:out value="${item.posicao}" />
                                </td>
                                <td>#
                                    <c:out value="${item.id}" />
                                </td>
                                <td>
                                    <c:out value="${item.nomeCliente}" />
                                </td>
                                <td>
                                    <c:out value="${item.nomePet}" />
                                </td>
                                <td>
                                    <c:out value="${item.servico}" />
                                </td>
                                <td>
                                    <c:out value="${item.horarioChegada}" />
                                </td>
                                <td>
                                    <c:out value="${item.esperaMinutos}" /> min
                                </td>
                                <td><span class="badge ${item.statusClass}">
                                        <c:out value="${item.status}" />
                                    </span></td>
                                <td><form action="${pageContext.request.contextPath}/admin/fila/excluir" method="post"><input type="hidden" name="id" value="${item.id}"><button class="btn btn-perigo btn-sm" onclick="return confirm('Remover atendimento?')">Remover</button></form></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty fila}">
                            <tr>
                                <td colspan="9" style="text-align:center;padding:2rem">A fila está vazia.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </main>
        <script>document.querySelector('form[action$="/admin/fila/salvar"]').addEventListener('submit',e=>document.getElementById('servicoNome').value=e.target.servico.selectedOptions[0].text);</script></body>

        </html>
