<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Veterinário - Smart Pet</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .secao { background:#fff; border:1px solid #e0e0e0; border-radius:10px; padding:1.4rem; margin-bottom:1.4rem; }
        .secao h3 { margin-top:0; color:#333; border-bottom:2px solid #e8e8e8; padding-bottom:0.6rem; }
        .perfil { display:inline-block; background:#e8f5e9; color:#2e7d32; padding:0.3rem 0.9rem;
                  border-radius:16px; font-size:0.8rem; font-weight:600; margin-bottom:1rem; }
        .vazio { text-align:center; color:#999; padding:2rem; }
        .ficha { border:1px solid #e0e0e0; border-left:4px solid #2e7d32; border-radius:8px;
                 padding:1rem 1.2rem; margin-bottom:1rem; background:#fafafa; }
        .ficha h4 { margin:0 0 0.4rem 0; font-size:1.05rem; }
        .ficha .linha { color:#666; font-size:0.88rem; margin:0.15rem 0; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet — Veterinário</div>
        <nav>
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila completa</a>
            <a href="/logout" style="color:#ffcdd2;">Sair</a>
        </nav>
    </header>
    <main class="container">
        <span class="perfil">🩺 Perfil: Veterinário / Atendente</span>
        <h2>Atendimentos</h2>
        <p style="color:#666; margin-bottom:1.4rem;">
            Aqui ficam os pets chamados pelo balcão. Conclua quando o atendimento terminar.
        </p>

        <c:if test="${not empty mensagem}">
            <div style="background:#e8f5e9;border:1px solid #a5d6a7;color:#2e7d32;padding:1rem 1.2rem;border-radius:8px;margin-bottom:1.4rem;font-weight:600;">
                ✅ ${mensagem}
            </div>
        </c:if>
        <c:if test="${not empty erro}">
            <div style="background:#ffebee;border:1px solid #ef9a9a;color:#c62828;padding:1rem 1.2rem;border-radius:8px;margin-bottom:1.4rem;font-weight:600;">
                ⚠️ ${erro}
            </div>
        </c:if>

        <!-- EM ATENDIMENTO -->
        <div class="secao">
            <h3>Em atendimento agora</h3>

            <c:set var="temAtendimento" value="false" />
            <c:forEach var="item" items="${fila}">
                <c:if test="${item.status == 'Em atendimento'}">
                    <c:set var="temAtendimento" value="true" />
                    <div class="ficha">
                        <h4>🐾 ${item.nomePet}</h4>
                        <p class="linha">Tutor: <strong>${item.nomeCliente}</strong></p>
                        <p class="linha">Serviço: ${item.servico}</p>
                        <p class="linha">Chegou às ${item.horarioChegada} · esperou ${item.minutosEspera} min</p>
                        <form action="/fila/acao" method="POST" style="margin-top:0.8rem;">
                            <input type="hidden" name="acao" value="concluir">
                            <input type="hidden" name="id" value="${item.id}">
                            <button type="submit" class="btn btn-sucesso" style="padding:0.7rem 1.5rem;">
                                ✅ Concluir atendimento
                            </button>
                        </form>
                    </div>
                </c:if>
            </c:forEach>

            <c:if test="${temAtendimento == 'false'}">
                <p class="vazio">Nenhum pet em atendimento. Peça ao balcão para chamar o próximo.</p>
            </c:if>
        </div>

        <!-- AGUARDANDO -->
        <div class="secao">
            <h3>Na fila aguardando o balcão chamar</h3>
            <table class="tabela">
                <thead>
                    <tr>
                        <th>Pet</th>
                        <th>Cliente</th>
                        <th>Serviço</th>
                        <th>Chegada</th>
                        <th>Espera</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="temEspera" value="false" />
                    <c:forEach var="item" items="${fila}">
                        <c:if test="${item.status == 'Em espera'}">
                            <c:set var="temEspera" value="true" />
                            <tr>
                                <td><strong>${item.nomePet}</strong></td>
                                <td>${item.nomeCliente}</td>
                                <td>${item.servico}</td>
                                <td>${item.horarioChegada}</td>
                                <td>${item.minutosEspera} min</td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${temEspera == 'false'}">
                        <tr><td colspan="5" class="vazio">Fila de espera vazia.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <!-- HISTÓRICO -->
        <div class="secao">
            <h3>Histórico do dia (concluídos e cancelados)</h3>
            <table class="tabela">
                <thead>
                    <tr>
                        <th>Pet</th>
                        <th>Cliente</th>
                        <th>Serviço</th>
                        <th>Status</th>
                        <th>Observação</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="temHist" value="false" />
                    <c:forEach var="item" items="${fila}">
                        <c:if test="${item.status == 'Concluído' || item.status == 'Cancelado' || item.status == 'Finalizado'}">
                            <c:set var="temHist" value="true" />
                            <tr>
                                <td>${item.nomePet}</td>
                                <td>${item.nomeCliente}</td>
                                <td>${item.servico}</td>
                                <td><span class="badge ${item.statusClass}">${item.status}</span></td>
                                <td style="color:#888; font-size:0.85rem;">${item.motivoCancelamento}</td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${temHist == 'false'}">
                        <tr><td colspan="5" class="vazio">Nenhum atendimento encerrado ainda.</td></tr>
                    </c:if>
                </tbody>
            </table>
            <p style="font-size:0.75rem; color:#999; margin-top:0.8rem;">
                Os registros encerrados não são apagados: ficam aqui como histórico.
            </p>
        </div>
    </main>
</body>
</html>
