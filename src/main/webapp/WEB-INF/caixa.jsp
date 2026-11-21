<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="pt-br">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Caixa - SmartPet</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            <style>
                .cards {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 1rem;
                    margin: 1.2rem 0;
                }

                .card-num {
                    background: #fff;
                    border: 1px solid #e0e0e0;
                    border-radius: 10px;
                    padding: 1rem 1.2rem;
                }

                .card-num .rot {
                    color: #777;
                    font-size: 0.8rem;
                    text-transform: uppercase;
                    letter-spacing: .04em;
                }

                .card-num .val {
                    font-size: 1.5rem;
                    font-weight: 700;
                    margin-top: .3rem;
                }

                .verde {
                    color: #2e7d32;
                }

                .vermelho {
                    color: #c62828;
                }

                .form-linha {
                    display: flex;
                    gap: .6rem;
                    flex-wrap: wrap;
                    align-items: end;
                    margin-bottom: .6rem;
                }

                .form-linha .form-group {
                    margin: 0;
                }

                .secao {
                    background: #fff;
                    border: 1px solid #e0e0e0;
                    border-radius: 10px;
                    padding: 1.2rem;
                    margin-bottom: 1.2rem;
                }

                .secao h3 {
                    margin-top: 0;
                    border-bottom: 2px solid #eee;
                    padding-bottom: .5rem;
                }
            </style>
        </head>

        <body>
            <%@ include file="menu.jspf" %>
                <main class="container">
                    <h2>Controle financeiro (RF07)</h2>
                    <p style="color:#666">Receitas e despesas operacionais do Pet Shop, com saldo calculado
                        automaticamente.</p>

                    <c:if test="${not empty mensagem}">
                        <div style="background:#e8f5e9;border:1px solid #a5d6a7;color:#2e7d32;padding:.8rem 1rem;border-radius:8px;margin-bottom:1rem;font-weight:600;">
                            ✅
                            <c:out value="${mensagem}" />
                        </div>
                    </c:if>
                    <c:if test="${not empty erro}">
                        <div style="background:#ffebee;border:1px solid #ef9a9a;color:#c62828;padding:.8rem 1rem;border-radius:8px;margin-bottom:1rem;font-weight:600;">
                            ⚠️
                            <c:out value="${erro}" />
                        </div>
                    </c:if>

                    <div class="cards">
                        <div class="card-num">
                            <div class="rot">Receitas</div>
                            <div class="val verde">R$ ${totalReceitas}</div>
                        </div>
                        <div class="card-num">
                            <div class="rot">Despesas</div>
                            <div class="val vermelho">R$ ${totalDespesas}</div>
                        </div>
                        <div class="card-num">
                            <div class="rot">Saldo final</div>
                            <div class="val">R$ ${saldo}</div>
                        </div>
                    </div>

                    <div class="secao">
                        <h3>Novo lançamento</h3>
                        <form action="${pageContext.request.contextPath}/admin/caixa/movimento" method="POST">
                            <div class="form-linha">
                                <div class="form-group">
                                    <label>Tipo</label>
                                    <select name="tipo">
                                        <option value="receita">Receita</option>
                                        <option value="despesa">Despesa</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Categoria</label>
                                    <input type="text" name="categoria" required placeholder="Ex: Banho e tosa">
                                </div>
                                <div class="form-group" style="flex:1;min-width:200px;">
                                    <label>Descrição</label>
                                    <input type="text" name="descricao" required placeholder="Ex: Venda de ração">
                                </div>
                                <div class="form-group">
                                    <label>Valor (R$)</label>
                                    <input type="text" name="valor" required placeholder="0,00" style="width:110px;">
                                </div>
                                <button class="btn btn-sucesso">Lançar</button>
                            </div>
                        </form>
                    </div>

                    <div class="secao">
                        <h3>Receitas</h3>
                        <table class="tabela">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Categoria</th>
                                    <th>Descrição</th>
                                    <th>Valor</th>
                                    <th>Data</th>
                                    <th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="r" items="${receitas}">
                                    <tr>
                                        <td>
                                            <c:out value="${r.id}" />
                                        </td>
                                        <td>
                                            <c:out value="${r.categoria}" />
                                        </td>
                                        <td>
                                            <c:out value="${r.descricao}" />
                                        </td>
                                        <td class="verde">R$ <c:out value="${r.valor}" /></td>
                                        <td>
                                            <c:out value="${r.data_hora}" />
                                        </td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/admin/caixa/excluir" method="POST">
                                                <input type="hidden" name="tipo" value="receita">
                                                <input type="hidden" name="id" value="${r.id}">
                                                <button class="btn btn-perigo btn-sm" onclick="return confirm('Remover lançamento?')">Remover</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty receitas}">
                                    <tr>
                                        <td colspan="6" style="text-align:center;color:#888;padding:1.5rem">Nenhuma receita lançada.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <div class="secao">
                        <h3>Despesas</h3>
                        <table class="tabela">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Categoria</th>
                                    <th>Descrição</th>
                                    <th>Valor</th>
                                    <th>Data</th>
                                    <th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="d" items="${despesas}">
                                    <tr>
                                        <td>
                                            <c:out value="${d.id}" />
                                        </td>
                                        <td>
                                            <c:out value="${d.categoria}" />
                                        </td>
                                        <td>
                                            <c:out value="${d.descricao}" />
                                        </td>
                                        <td class="vermelho">R$ <c:out value="${d.valor}" /></td>
                                        <td>
                                            <c:out value="${d.data_hora}" />
                                        </td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/admin/caixa/excluir" method="POST">
                                                <input type="hidden" name="tipo" value="despesa">
                                                <input type="hidden" name="id" value="${d.id}">
                                                <button class="btn btn-perigo btn-sm" onclick="return confirm('Remover lançamento?')">Remover</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty despesas}">
                                    <tr>
                                        <td colspan="6" style="text-align:center;color:#888;padding:1.5rem">Nenhuma despesa lançada.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </main>
        </body>

        </html>
