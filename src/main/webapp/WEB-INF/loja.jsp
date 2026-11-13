<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Loja - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <header class="navbar">
                <div class="logo">🐾 SmartPet Admin</div>
                <nav><a href="${pageContext.request.contextPath}/fila">Fila</a><a class="active"
                        href="${pageContext.request.contextPath}/loja">Loja</a><a
                        href="${pageContext.request.contextPath}/pets">Cadastro de pets</a><a
                        href="${pageContext.request.contextPath}/avaliacoes">Avaliações</a><a
                        href="${pageContext.request.contextPath}/logout">Sair</a></nav>
            </header>
            <main class="container">
                <h2>Loja e produtos</h2>
                <p>Catálogo integrado ao aplicativo SmartPet. Cole um link direto da imagem para pré-visualizar o produto.</p>
                <form action="${pageContext.request.contextPath}/admin/produtos/salvar" method="post" class="card" style="margin:18px 0;display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:10px"><input type="hidden" name="id"><input name="nome" placeholder="Produto" required><input name="marca" placeholder="Marca"><input name="categoria" placeholder="Categoria"><input name="estoque" type="number" min="0" placeholder="Estoque" required><input name="preco" type="number" min="0" step="0.01" placeholder="Preço" required><input name="url" id="imageUrl" type="url" placeholder="URL da imagem" oninput="preview.src=this.value"><img id="preview" alt="Prévia" style="width:90px;max-height:70px;object-fit:contain"><button class="btn btn-sucesso">Salvar produto</button></form>
                <table class="tabela">
                    <thead>
                        <tr>
                            <th>Código</th>
                            <th>Produto</th>
                            <th>Marca</th>
                            <th>Categoria</th>
                            <th>Estoque</th>
                            <th>Preço</th><th>Imagem</th><th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prod" items="${produtos}">
                            <tr>
                                <td>#
                                    <c:out value="${prod.codigo}" />
                                </td>
                                <td>
                                    <c:out value="${prod.nome}" />
                                </td>
                                <td>
                                    <c:out value="${prod.marca}" />
                                </td>
                                <td>
                                    <c:out value="${prod.categoria}" />
                                </td>
                                <td>
                                    <c:out value="${prod.estoque}" />
                                </td>
                                <td>R$
                                    <c:out value="${prod.preco}" />
                                </td>
                                <td><c:if test="${not empty prod.url}"><img src="<c:out value='${prod.url}'/>" alt="Produto" style="width:56px;height:48px;object-fit:contain"></c:if></td>
                                <td><form action="${pageContext.request.contextPath}/admin/produtos/excluir" method="post"><input type="hidden" name="id" value="${prod.codigo}"><button class="btn btn-perigo btn-sm" onclick="return confirm('Excluir produto?')">Excluir</button></form></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty produtos}">
                            <tr>
                                <td colspan="8" style="text-align:center;padding:2rem">Nenhum produto cadastrado.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </main>
        <script>const preview=document.getElementById('preview');</script></body>

        </html>
