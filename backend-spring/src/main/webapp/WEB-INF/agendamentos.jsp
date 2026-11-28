<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="pt-br">

            <head>
                <meta charset="UTF-8">
                <title>Agendamentos - Smart Pet Admin</title>

                <link rel="stylesheet" href="/css/style.css">

                <style>
                    .filtros {
                        display: flex;
                        gap: 15px;
                        align-items: end;
                        margin-bottom: 20px;
                        flex-wrap: wrap;
                    }

                    .filtros input,
                    .filtros select {
                        padding: 8px;
                        border: 1px solid #ccc;
                        border-radius: 6px;
                    }

                    .badge {
                        padding: 6px 10px;
                        border-radius: 5px;
                        color: white;
                        font-size: 0.85rem;
                        font-weight: bold;
                    }

                    .agendado {
                        background-color: #ceab37;
                    }

                    .confirmado {
                        background-color: #2e7d32;
                    }

                    .cancelado {
                        background-color: #c62828;
                    }

                    .concluido {
                        background-color: #2e7d32;
                    }

                    .resumo {
                        margin-bottom: 20px;
                        color: #666;
                    }
                </style>

            </head>

            <body>

                <header class="navbar">
                    <div class="logo">🐾 Smart Pet Admin</div>

                    <nav>
                        <a href="/balconista">Balcão</a>
                        <a href="/veterinario">Veterinário</a>
                        <a href="/fila">Fila</a>
                        <a href="admin/agendamentos">Agendamentos</a>
                        <a href="/pets">Pets</a>
                        <a href="/clientes">Clientes</a>
                        <a href="/loja">Loja</a>
                        <a href="/vendas">Vendas</a>
                        <a href="/caixa">Caixa</a>
                        <a href="/avaliacoes">Avaliações</a>
                        <a href="/notificacoes">Notificações</a>
                        <a href="/logout" style="color:#ffcdd2;">Sair</a>
                    </nav>
                </header>

                <br><br>

                <main class="container">

                    <h2>📅 Agendamentos</h2>

                    <p class="resumo">
                        Visualização de todos os agendamentos com hora marcada cadastrados no sistema através do app
                        mobile.
                    </p>

                    <!-- FILTROS VISUAIS -->
                    <div class="filtros">

                        <div>
                            <label>Data</label><br>
                            <input type="date" id="filtroData">
                        </div>

                        <div>
                            <label>Status</label><br>
                            <select id="filtroStatus">
                                <option value="">Todos</option>
                                <option value="Agendado">Agendado</option>
                                <option value="Confirmado">Confirmado</option>
                                <option value="Cancelado">Cancelado</option>
                                <option value="Concluido">Concluído</option>
                            </select>
                        </div>

                    </div>

                    <table class="tabela" id="tabelaAgendamentos">

                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Data</th>
                                <th>Hora</th>
                                <th>CPF</th>
                                <th>Pet</th>
                                <th>Serviço</th>
                                <th>Categoria</th>
                                <th>Status</th>
                                <th>Ações</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:forEach var="agenda" items="${agendamentos}">
                                <tr>

                                    <td>#${agenda.id}</td>

                                    <td>
                                        <fmt:parseDate value="${agenda.data_agendamento}" pattern="yyyy-MM-dd"
                                            var="dataFormatada" />

                                        <fmt:formatDate value="${dataFormatada}" pattern="dd/MM/yyyy" />
                                    </td>

                                    <td>${agenda.hora_agendamento}</td>

                                    <td>${agenda.cpf}</td>

                                    <td>${agenda.nomePet}</td>

                                    <td>${agenda.servico}</td>

                                    <td>${agenda.categoria}</td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${agenda.status == 'Agendado'}">
                                                <span class="badge agendado">
                                                    ${agenda.status}
                                                </span>
                                            </c:when>

                                            <c:when test="${agenda.status == 'Confirmado'}">
                                                <span class="badge confirmado">
                                                    ${agenda.status}
                                                </span>
                                            </c:when>

                                            <c:when test="${agenda.status == 'Cancelado'}">
                                                <span class="badge cancelado">
                                                    ${agenda.status}
                                                </span>
                                            </c:when>
                                            <c:when test="${agenda.status == 'Concluido'}">
                                                <span class="badge concluido">
                                                    CONCLUÍDO
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge">${agenda.status}</span>
                                            </c:otherwise>

                                        </c:choose>

                                    </td>
                                    <td>

                                        <c:if test="${agenda.status ne 'Concluido' and agenda.status ne 'Cancelado'}">

                                            <form action="/admin/agendamentos/${agenda.id}/concluir" method="POST"
                                                style="margin:0;">

                                                <button type="submit" class="btn btn-sucesso"
                                                    onclick="return confirm('Deseja marcar este agendamento como concluído?')">
                                                    ✓ Concluir
                                                </button>

                                            </form>

                                        </c:if>

                                        <c:if test="${agenda.status == 'Concluido'}">
                                            <span style="color:#2e7d32;font-weight:600;">
                                                
                                            </span>
                                        </c:if>

                                        <c:if test="${agenda.status == 'Cancelado'}">
                                            <span style="color:#999;">
                                                
                                            </span>
                                        </c:if>

                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty agendamentos}">
                                <tr>
                                    <td colspan="8" style="text-align:center;padding:30px;">
                                        Nenhum agendamento encontrado.
                                    </td>
                                </tr>
                            </c:if>

                        </tbody>

                    </table>

                </main>

                <script>

                    document.getElementById("filtroData").addEventListener("change", filtrar);
                    document.getElementById("filtroStatus").addEventListener("change", filtrar);

                    function filtrar() {

                        const data = document.getElementById("filtroData").value;
                        const status = document.getElementById("filtroStatus").value.toLowerCase();

                        const linhas = document.querySelectorAll("#tabelaAgendamentos tbody tr");

                        linhas.forEach(linha => {

                            const dataTabela = linha.cells[1]?.textContent.trim();
                            const statusTabela = linha.cells[7]?.textContent.trim().toLowerCase();

                            let mostrar = true;

                            if (data && dataTabela !== data) {
                                mostrar = false;
                            }

                            if (status && statusTabela !== status) {
                                mostrar = false;
                            }

                            linha.style.display = mostrar ? "" : "none";
                        });

                    }

                </script>

            </body>

            </html>