<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Loja / Produtos - Smart Pet Admin</title>
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
        <h2>Catálogo de Produtos (Loja Virtual & Física)</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Produtos sincronizados diretamente com a aba Loja do aplicativo móvel.</p>

        <div class="split-view">
            <div>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>#Cód</th>
                            <th>Produto</th>
                            <th>Marca</th>
                            <th>Categoria</th>
                            <th>Estoque</th>
                            <th>Preço (R$)</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prod" items="${produtos}">
                            <tr>
                                <td><strong>#${prod.codigo}</strong></td>
                                <td>${prod.nome}</td>
                                <td>${prod.marca}</td>
                                <td><span class="badge" style="background:#e3f2fd; color:#1565c0;">${prod.categoria}</span></td>
                                <td><strong>${prod.estoque} un</strong></td>
                                <td style="color:#2e7d32; font-weight:bold;">R$ ${prod.preco}</td>
                                <td>
                                    <form action="/admin/produtos/excluir" method="POST" style="display:inline;">
                                        <input type="hidden" name="codigo" value="${prod.codigo}">
                                        <button type="submit" class="btn btn-cancelar btn-sm" onclick="return confirm('Deseja excluir este produto?')">Excluir</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty produtos}">
                            <tr>
                                <td colspan="7" style="text-align: center; color: #888; padding: 2rem;">Nenhum produto cadastrado na loja.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <div class="card-form">
                <h3>➕ Cadastrar Novo Produto</h3>
                <p style="color: #666; font-size: 0.85rem; margin-bottom: 1rem;">O produto cadastrado fica visível no app.</p>
                <form action="/admin/produtos/salvar" method="POST">
                    <div class="form-group">
                        <label>Nome do Produto</label>
                        <input type="text" name="nome" required placeholder="Ex: Ração Premier 15kg">
                    </div>
                    <div class="form-group">
                        <label>Marca / Fabricante</label>
                        <input type="text" name="marca" placeholder="Ex: Premier Pet">
                    </div>
                    <div class="form-group">
                        <label>Categoria</label>
                        <select name="categoria">
                            <option value="Alimento">Alimento / Ração</option>
                            <option value="Higiene">Higiene & Beleza</option>
                            <option value="Acessórios">Acessórios</option>
                            <option value="Brinquedos">Brinquedos</option>
                            <option value="Medicamentos">Medicamentos</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Quantidade em Estoque</label>
                        <input type="number" name="estoque" required value="10" min="0">
                    </div>
                    <div class="form-group">
                        <label>Preço de Venda (R$)</label>
                        <input type="number" step="0.01" name="preco" required placeholder="Ex: 89.90">
                    </div>
                    <button type="submit" class="btn btn-sucesso" style="width: 100%; padding: 0.75rem; margin-top: 0.5rem;">Cadastrar Produto</button>
                </form>
            </div>
        </div>
    </main>
</body>
</html>