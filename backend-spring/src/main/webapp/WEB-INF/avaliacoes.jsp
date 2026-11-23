<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Avaliações - Smart Pet Admin</title>
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
        <h2>Feedback & Avaliações dos Clientes</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Avaliações enviadas pelos clientes pelo aplicativo mobile após os atendimentos.</p>

        <div style="display:flex;gap:.5rem;margin-bottom:1rem;">
            <button type="button" class="btn btn-sucesso" onclick="filtrarAvaliacoes(false, this)">Todas</button>
            <button type="button" class="btn" onclick="filtrarAvaliacoes(true, this)">Comentários</button>
        </div>

        <table class="tabela">
            <thead>
                <tr>
                    <th>#ID</th>
                    <th>Pet</th>
                    <th>Serviço / Descrição</th>
                    <th>Nota (1 a 3)</th>
                    <th>Comentário</th>
                    <th>Status</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="a" items="${avaliacoes}">
                    <tr data-tem-comentario="${not empty a.comentario}">
                        <td><strong>#${a.idAval}</strong></td>
                        <td>🐾 ${a.nomePet}</td>
                        <td>${a.descricao}</td>
                        <td>
                            <c:choose>
                                <c:when test="${a.nota eq 3}"><span style="color:#2e7d32; font-weight:bold;">⭐⭐⭐ Excelente</span></c:when>
                                <c:when test="${a.nota eq 2}"><span style="color:#f57f17; font-weight:bold;">⭐⭐ Bom</span></c:when>
                                <c:when test="${a.nota eq 1}"><span style="color:#c62828; font-weight:bold;">⭐ Ruim</span></c:when>
                                <c:otherwise><span style="color:#888;">Pendente de avaliação</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${a.comentario}</td>
                        <td>
                            <c:if test="${a.respondida}">
                                <span class="badge badge-concluido">Respondida</span>
                            </c:if>
                            <c:if test="${not a.respondida}">
                                <span class="badge badge-espera">Aguardando Cliente</span>
                            </c:if>
                        </td>
                        <td>
                            <form action="/admin/avaliacoes/excluir" method="POST" style="display:inline;">
                                <input type="hidden" name="id" value="${a.idAval}">
                                <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja excluir esta avaliação?')">Excluir</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty avaliacoes}">
                    <tr>
                        <td colspan="7" style="text-align: center; color: #888; padding: 2rem;">Nenhuma avaliação registrada ainda.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </main>
    <script>
        function filtrarAvaliacoes(comComentario, botao) {
            document.querySelectorAll('tbody tr[data-tem-comentario]').forEach(function (linha) {
                linha.hidden = comComentario && linha.dataset.temComentario !== 'true';
            });
            botao.parentElement.querySelectorAll('button').forEach(function (item) {
                item.classList.remove('btn-sucesso');
            });
            botao.classList.add('btn-sucesso');
        }
    </script>
</body>
</html>
