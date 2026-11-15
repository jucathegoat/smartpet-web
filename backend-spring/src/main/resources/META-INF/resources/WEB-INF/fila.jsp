<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Fila de Atendimento - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <meta http-equiv="refresh" content="5">
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
        <h2>Fila de Atendimento (Integrada com App Flutter)</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Novos agendamentos feitos no aplicativo entram diretamente nesta lista em tempo real.</p>

        <div class="acoes-topo" style="display: flex; gap: 10px; margin-bottom: 1.5rem;">
            <form action="/fila/acao" method="POST">
                <input type="hidden" name="acao" value="chamar">
                <button type="submit" class="btn btn-sucesso">📢 Chamar Próximo Paciente</button>
            </form>
            <a href="/admin/agendamentos/novo" class="btn" style="background-color: #2e7d32; color: #fff; text-decoration: none; padding: 10px 18px; border-radius: 6px; font-weight: 600;">➕ Novo Agendamento Manual</a>
        </div>

        <table class="tabela">
            <thead>
                <tr>
                    <th>#ID</th>
                    <th>Cliente / Tutor</th>
                    <th>Pet</th>
                    <th>Serviço</th>
                    <th>Chegada</th>
                    <th>Espera</th>
                    <th>Status</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${fila}">
                    <tr>
                        <td><strong>#${item.id}</strong></td>
                        <td>${item.nomeCliente}</td>
                        <td>${item.nomePet}</td>
                        <td>${item.servico}</td>
                        <td>${item.horarioChegada}</td>
                        <td>${item.minutosEspera} min</td>
                        <td><span class="badge ${item.statusClass}">${item.status}</span></td>
                        <td>
                            <c:if test="${item.status eq 'Em espera'}">
                                <form action="/fila/acao" method="POST" style="display:inline;">
                                    <input type="hidden" name="acao" value="chamar">
                                    <button type="submit" class="btn btn-sucesso btn-sm">Atender</button>
                                </form>
                            </c:if>
                            <c:if test="${item.status eq 'Em atendimento'}">
                                <form action="/fila/acao" method="POST" style="display:inline;">
                                    <input type="hidden" name="acao" value="concluir">
                                    <input type="hidden" name="id" value="${item.id}">
                                    <button type="submit" class="btn btn-sucesso btn-sm">Concluir</button>
                                </form>
                            </c:if>
                            <c:if test="${item.status eq 'Em espera'}">
                                <form action="/fila/acao" method="POST" style="display:inline;">
                                    <input type="hidden" name="acao" value="cancelar">
                                    <input type="hidden" name="id" value="${item.id}">
                                    <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja cancelar este agendamento?')">Cancelar</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty fila}">
                    <tr>
                        <td colspan="8" style="text-align: center; color: #888; padding: 2rem;">A fila de atendimento está vazia no momento.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </main>
</body>
</html>