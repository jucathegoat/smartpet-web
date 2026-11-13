<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Pets - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            <style>
                .split {
                    display: grid;
                    grid-template-columns: 2fr 1fr;
                    gap: 1.5rem
                }

                .card {
                    background: #fff;
                    padding: 1.5rem;
                    border-radius: 8px;
                    box-shadow: 0 4px 12px #0001
                }

                .card label {
                    display: block;
                    margin: .8rem 0 .25rem;
                    font-weight: 600
                }

                .card input,
                .card select {
                    width: 100%;
                    padding: .65rem;
                    border: 1px solid #ccc;
                    border-radius: 6px
                }

                @media(max-width:800px) {
                    .split {
                        grid-template-columns: 1fr
                    }

                    .tabela {
                        font-size: .8rem
                    }
                }
            </style>
        </head>

        <body>
            <header class="navbar">
                <div class="logo">🐾 SmartPet Admin</div>
                <nav><a href="${pageContext.request.contextPath}/fila">Fila</a><a
                        href="${pageContext.request.contextPath}/loja">Loja</a><a class="active"
                        href="${pageContext.request.contextPath}/pets">Cadastro de pets</a><a
                        href="${pageContext.request.contextPath}/avaliacoes">Avaliações</a><a
                        href="${pageContext.request.contextPath}/logout">Sair</a></nav>
            </header>
            <main class="container">
                <h2>Cadastro de pets</h2>
                <div class="split">
                    <section>
                        <table class="tabela">
                            <thead>
                                <tr>
                                    <th>Pet</th>
                                    <th>Espécie</th>
                                    <th>Raça</th>
                                    <th>Idade</th>
                                    <th>Tutor</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${pets}">
                                    <tr>
                                        <td>#
                                            <c:out value="${p.id}" /> ·
                                            <c:out value="${p.nome}" />
                                        </td>
                                        <td>
                                            <c:out value="${p.especie}" />
                                        </td>
                                        <td>
                                            <c:out value="${p.raca}" />
                                        </td>
                                        <td>
                                            <c:out value="${p.idade}" /> anos
                                        </td>
                                        <td>
                                            <c:out value="${p.nomeCliente}" /><br><small>
                                                <c:out value="${p.cpfCliente}" />
                                            </small>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty pets}">
                                    <tr>
                                        <td colspan="5" style="text-align:center;padding:2rem">Nenhum pet cadastrado.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </section>
                    <form class="card" action="${pageContext.request.contextPath}/admin/pets/salvar" method="post">
                        <h3>Novo pet</h3><label for="cpfCliente">Tutor</label><select id="cpfCliente" name="cpfCliente"
                            required>
                            <option value="">Selecione o tutor</option>
                            <c:forEach var="cliente" items="${clientes}">
                                <option value="${cliente.cpf}">
                                    <c:out value="${cliente.nome}" /> —
                                    <c:out value="${cliente.cpf}" />
                                </option>
                            </c:forEach>
                        </select><label for="nome">Nome</label><input id="nome" name="nome" required
                            maxlength="100"><label for="especie">Espécie</label><select id="especie" name="especie"
                            required>
                            <option>Cachorro</option>
                            <option>Gato</option>
                            <option>Pássaro</option>
                            <option>Outro</option>
                        </select><label for="raca">Raça</label><input id="raca" name="raca" maxlength="50"><label
                            for="idade">Idade em anos</label><input id="idade" name="idade" type="number" min="0"
                            max="50" value="1" required><button class="btn btn-sucesso" type="submit"
                            style="width:100%;margin-top:1rem">Cadastrar pet</button>
                    </form>
                </div>
            </main>
        </body>

        </html>