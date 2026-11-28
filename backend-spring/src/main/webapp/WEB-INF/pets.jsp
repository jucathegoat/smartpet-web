<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Pets - Smart Pet Admin</title>
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
        .form-group input, .form-group select { width: 100%; padding: 0.65rem; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
        <nav>
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila</a>
            <a href="/admin/agendamentos">Agendamentos</a>
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
        <h2>Gerenciamento de Pets (CRUD)</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Pets cadastrados no sistema (Mobile & Web).</p>

        <div class="split-view">
            <div>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Nome do Pet</th>
                            <th>Espécie</th>
                            <th>Raça</th>
                            <th>Idade</th>
                            <th>Documento do pet</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${pets}">
                            <tr>
                                <td><strong>#${p.id}</strong></td>
                                <td>🐾 ${p.nome}</td>
                                <td>${p.especie}</td>
                                <td>${p.raca}</td>
                                <td>${p.idade} anos</td>
                                <td>${p.nDocumento}</td>
                                <td>
                                    <form action="/admin/pets/excluir" method="POST" style="display:inline;">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja excluir este pet?')">Excluir</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty pets}">
                            <tr>
                                <td colspan="7" style="text-align: center; color: #888; padding: 2rem;">Nenhum pet cadastrado.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <div class="card-form">
                <h3>➕ Cadastrar / Atualizar Pet</h3>
                <p style="color: #666; font-size: 0.85rem; margin-bottom: 1rem;">Adicione um animal vinculado a um tutor.</p>
                <form action="/admin/pets/salvar" method="POST">
                    <div class="form-group">
                        <label>Tutor Responsável</label>
                        <select name="cpfCliente" required>
                            <option value="">Selecione o Cliente</option>
                            <c:forEach var="c" items="${clientes}">
                                <option value="${c.cpf}">${c.nome} (${c.cpf})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Nome do Pet</label>
                        <input type="text" name="nome" required placeholder="Ex: Rex">
                    </div>
                    <div class="form-group">
                        <label>Espécie</label>
                        <select name="especie" required>
                            <option value="Cachorro">Cachorro</option>
                            <option value="Gato">Gato</option>
                            <option value="Pássaro">Pássaro</option>
                            <option value="Outro">Outro</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Raça</label>
                        <input type="text" name="raca" placeholder="Ex: Poodle / Persa">
                    </div>
                    <div class="form-group">
                        <label>Idade (em anos)</label>
                        <input type="number" name="idade" value="1" min="0" max="30">
                    </div>
                    <button type="submit" class="btn btn-sucesso" style="width: 100%; padding: 0.75rem; margin-top: 0.5rem;">Salvar Pet</button>
                </form>
            </div>
        </div>
    </main>
</body>
</html>
