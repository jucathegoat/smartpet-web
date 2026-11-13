<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Notificações - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .split-view {
            display: grid;
            grid-template-columns: 2fr 1.2fr;
            gap: 2rem;
        }
        .card-form {
            background: #fff;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.06);
            height: fit-content;
        }
        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; margin-bottom: 0.3rem; font-weight: 600; font-size: 0.9rem; }
        .form-group input, .form-group textarea { width: 100%; padding: 0.65rem; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
        <nav>
            <a href="/fila">Fila</a>
            <a href="/pets">Pets</a>
            <a href="/clientes">Clientes</a>
            <a href="/loja">Loja</a>
            <a href="/avaliacoes">Avaliações</a>
            <a href="/notificacoes">Notificações</a>
            <a href="/logout" style="color: #ffcdd2;">Sair</a>
        </nav>
    </header>
    <main class="container">
        <h2>Disparo de Notificações para o App Mobile</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Envie comunicados, lembretes e promoções diretamente para os clientes.</p>

        <div class="split-view">
            <div>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Título</th>
                            <th>Mensagem</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="n" items="">
                            <tr>
                                <td><strong>#</strong></td>
                                <td>🔔 </td>
                                <td></td>
                                <td>
                                    <form action="/admin/notificacoes/excluir" method="POST" style="display:inline;">
                                        <input type="hidden" name="id" value="">
                                        <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja excluir este aviso?')">Excluir</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="">
                            <tr>
                                <td colspan="4" style="text-align: center; color: #888; padding: 2rem;">Nenhuma notificação enviada.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <div class="card-form">
                <h3>📢 Enviar Nova Notificação</h3>
                <p style="color: #666; font-size: 0.85rem; margin-bottom: 1rem;">O comunicado aparecerá na tela Notificações do Flutter.</p>
                <form action="/admin/notificacoes/enviar" method="POST">
                    <div class="form-group">
                        <label>Título do Alerta</label>
                        <input type="text" name="titulo" required placeholder="Ex: Seu Pet está pronto!">
                    </div>
                    <div class="form-group">
                        <label>Mensagem</label>
                        <textarea name="mensagem" rows="4" required placeholder="Ex: O banho e tosa foi concluído com sucesso. Já pode vir retirar seu pet."></textarea>
                    </div>
                    <button type="submit" class="btn btn-sucesso" style="width: 100%; padding: 0.75rem; margin-top: 0.5rem;">Enviar Alerta</button>
                </form>
            </div>
        </div>
    </main>
</body>
</html>