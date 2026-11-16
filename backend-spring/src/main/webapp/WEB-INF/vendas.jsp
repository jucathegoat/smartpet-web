<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Vendas - Smart Pet Admin</title>
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
        <h2>Vendas de Produtos</h2>
        <p style="color: #666; margin-bottom: 1.5rem;">
            Cada venda debita o estoque e entra automaticamente como receita no caixa aberto (RF06 + RF07).
        </p>

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

        <div style="background:#f5f5f5; border-radius:8px; padding:1.2rem; margin-bottom:1.6rem;">
            <h3 style="margin-top:0;">➕ Registrar Venda</h3>
            <form action="/vendas/registrar" method="POST">
                <div class="form-group">
                    <label>Produto</label>
                    <select name="idProduto" required>
                        <option value="">Selecione o produto</option>
                        <c:forEach var="p" items="${produtos}">
                            <option value="${p.codigo}">${p.nome} — R$ ${p.preco} (${p.estoque} un)</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Quantidade</label>
                    <input type="number" name="quantidade" value="1" min="1" required>
                </div>
                <div class="form-group">
                    <label>Forma de Pagamento</label>
                    <select name="formaPagamento">
                        <option value="Dinheiro">Dinheiro</option>
                        <option value="Cartão de Crédito">Cartão de Crédito</option>
                        <option value="Cartão de Débito">Cartão de Débito</option>
                        <option value="Pix">Pix</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-sucesso" style="width:100%; padding:0.75rem; margin-top:0.5rem;">Registrar Venda</button>
            </form>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
            <h3 style="margin:0;">Histórico de Vendas</h3>
            <span style="color:#2e7d32; font-weight:bold;">Total: R$ ${totalVendido}</span>
        </div>

        <table class="tabela">
            <thead>
                <tr>
                    <th>#Venda</th>
                    <th>Produto</th>
                    <th>Qtd</th>
                    <th>Unitário</th>
                    <th>Total</th>
                    <th>Pagamento</th>
                    <th>Data / Hora</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="v" items="${vendas}">
                    <tr>
                        <td><strong>#${v.id}</strong></td>
                        <td>${v.produto}</td>
                        <td>${v.quantidade}</td>
                        <td>R$ ${v.precoUnitario}</td>
                        <td style="color:#2e7d32; font-weight:bold;">R$ ${v.total}</td>
                        <td>${v.formaPagamento}</td>
                        <td>${v.dataHora}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty vendas}">
                    <tr>
                        <td colspan="7" style="text-align: center; color: #888; padding: 2rem;">
                            Nenhuma venda registrada ainda.
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </main>
</body>
</html>
