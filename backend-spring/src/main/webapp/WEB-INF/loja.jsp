<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Loja - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .foto-produto {
            width: 56px; height: 56px; object-fit: cover;
            border-radius: 8px; border: 1px solid #e0e0e0;
            background: #f5f5f5; display: block;
        }
        .sem-foto {
            width: 56px; height: 56px; border-radius: 8px;
            border: 1px dashed #ccc; background: #fafafa;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.4rem; color: #bbb;
        }
        .preview {
            width: 70px; height: 70px; object-fit: cover;
            border-radius: 8px; border: 1px solid #ddd; display: none;
            margin-top: 0.5rem;
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
        <h2>Loja / Estoque</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">Produtos disponíveis para venda. Cole o link da foto e ela aparece na hora.</p>

        <c:if test="${not empty mensagem}">
            <div style="background:#e8f5e9; border:1px solid #a5d6a7; color:#2e7d32; padding:1rem 1.2rem; border-radius:8px; margin-bottom:1.4rem; font-weight:600;">
                ✅ ${mensagem}
            </div>
        </c:if>
        <c:if test="${not empty erro}">
            <div style="background:#ffebee; border:1px solid #ef9a9a; color:#c62828; padding:1rem 1.2rem; border-radius:8px; margin-bottom:1.4rem; font-weight:600;">
                ⚠️ ${erro}
            </div>
        </c:if>

        <div class="split-view">
            <div>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>Foto</th>
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
                                <td>
                                    <c:choose>
                                        <c:when test="${prod.temFoto}">
                                            <img class="foto-produto" src="${prod.url}" alt="${prod.nome}"
                                                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                                            <div class="sem-foto" style="display:none;">🐾</div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="sem-foto">🐾</div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
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
                                <td colspan="8" style="text-align: center; color: #888; padding: 2rem;">Nenhum produto cadastrado na loja.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <div class="card-form">
                <h3>➕ Cadastrar / Atualizar Produto</h3>
                <p style="color: #666; font-size: 0.85rem; margin-bottom: 1rem;">
                    Cole o link da imagem no campo Foto. Ela aparece na tabela e no app automaticamente.
                </p>
                <form action="/admin/produtos/salvar" method="POST">
                    <div class="form-group">
                        <label>Código (deixe vazio para novo)</label>
                        <input type="number" name="codigo" placeholder="Ex: 9">
                    </div>
                    <div class="form-group">
                        <label>Nome do Produto</label>
                        <input type="text" name="nome" required placeholder="Ex: Ração Golden 10kg">
                    </div>
                    <div class="form-group">
                        <label>Marca</label>
                        <input type="text" name="marca" placeholder="Ex: Golden">
                    </div>
                    <div class="form-group">
                        <label>Categoria</label>
                        <input type="text" name="categoria" placeholder="Ex: Ração / Higiene / Brinquedos">
                    </div>
                    <div class="form-group">
                        <label>Estoque (unidades)</label>
                        <input type="number" name="estoque" value="1" min="0" required>
                    </div>
                    <div class="form-group">
                        <label>Preço (R$)</label>
                        <input type="number" name="preco" step="0.01" min="0" required placeholder="Ex: 159.90">
                    </div>
                    <div class="form-group">
                        <label>Foto (link da imagem)</label>
                        <input type="text" name="url" id="campoUrl"
                               placeholder="https://exemplo.com/foto.jpg"
                               oninput="mostrarPreview(this.value)">
                        <img id="previewImg" class="preview" alt="Prévia da foto"
                             onerror="this.style.display='none';">
                    </div>
                    <button type="submit" class="btn btn-sucesso" style="width: 100%; padding: 0.75rem; margin-top: 0.5rem;">Salvar Produto</button>
                </form>
            </div>
        </div>
    </main>
    <script>
        // Mostra a foto na hora em que o link é colado
        function mostrarPreview(valor) {
            var img = document.getElementById('previewImg');
            if (valor && valor.trim().length > 8) {
                img.src = valor.trim();
                img.style.display = 'block';
            } else {
                img.style.display = 'none';
            }
        }
    </script>
</body>
</html>
