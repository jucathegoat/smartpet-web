<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Agendamentos - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <%@ include file="menu.jspf" %>
            <main class="container">
                <h2>Agendamentos</h2>
                <p>Horários marcados pelos tutores.</p>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>Data</th>
                            <th>Hora</th>
                            <th>Tutor</th>
                            <th>CPF</th>
                            <th>Pet</th>
                            <th>Serviço</th>
                            <th>Status</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${agendamentos}">
                            <tr>
                                <td>
                                    <c:out value="${a.data}" />
                                </td>
                                <td>
                                    <c:out value="${a.hora}" />
                                </td>
                                <td>
                                    <c:out value="${a.nomeCliente}" />
                                </td>
                                <td>
                                    <c:out value="${a.cpf}" />
                                </td>
                                <td>
                                    <c:out value="${a.nomePet}" />
                                </td>
                                <td>
                                    <c:out value="${a.servico}" />
                                </td>
                                <td><span class="badge badge-espera">
                                        <c:out value="${a.status}" />
                                    </span></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/agendamentos/excluir" method="post">
                                        <input type="hidden" name="id" value="${a.id}">
                                        <button class="btn btn-perigo btn-sm" onclick="return confirm('Remover agendamento?')">Remover</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty agendamentos}">
                            <tr>
                                <td colspan="8" style="text-align:center;padding:2rem">Nenhum agendamento.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </main>
        </body>

        </html>
