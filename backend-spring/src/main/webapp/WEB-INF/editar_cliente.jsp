<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Editar Cliente - Smart Pet Admin</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
        .secao { background: #fff; border: 1px solid #e0e0e0; border-radius: 10px; padding: 1.4rem; margin-bottom: 1.4rem; }
        .secao h3 { margin-top: 0; color: #333; border-bottom: 2px solid #e8e8e8; padding-bottom: 0.6rem; }
        .mini { font-size: 0.8rem; color: #888; }
        .chip { display: inline-block; background: #e3f2fd; color: #1565c0; padding: 0.3rem 0.7rem;
                border-radius: 14px; margin: 0.2rem; font-size: 0.85rem; }
        .linha-pet { border: 1px solid #eee; border-radius: 8px; padding: 0.9rem; margin-bottom: 0.9rem; background: #fafafa; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet Admin</div>
        <nav>
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila</a>
            <a href="/agendamentos">Agendamentos</a>
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
        <p><a href="/clientes" style="color:#1565c0;">← Voltar para clientes</a></p>
        <h2>Editar Cliente — RF02</h2>
        <p class="mini" style="margin-bottom:1.2rem;">
            CPF <strong>${cliente.cpf}</strong> · Status:
            <c:choose>
                <c:when test="${cliente.bloqueado}">
                    <span class="badge badge-cancelado">Bloqueado</span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-finalizado">Ativo</span>
                </c:otherwise>
            </c:choose>
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

        <!-- ============ DADOS DO CLIENTE ============ -->
        <div class="secao">
            <h3>Dados cadastrais</h3>
            <form action="/admin/clientes/editar" method="POST">
                <input type="hidden" name="cpf" value="${cliente.cpf}">
                <div class="form-group">
                    <label>Nome</label>
                    <input type="text" name="nome" value="${cliente.nome}" required>
                </div>
                <div class="grid2">
                    <div class="form-group">
                        <label>Telefone</label>
                        <input type="text" name="telefone" value="${cliente.telefone}" placeholder="(11) 99999-9999">
                    </div>
                    <div class="form-group">
                        <label>E-mail</label>
                        <input type="email" name="email" value="${cliente.email}">
                    </div>
                </div>
                <div class="form-group">
                    <label>Endereço</label>
                    <input type="text" name="endereco" value="${cliente.endereco}">
                </div>
                <button type="submit" class="btn btn-sucesso" style="padding:0.7rem 1.4rem;">Salvar dados</button>
            </form>
        </div>

        <!-- ============ PETS ============ -->
        <div class="secao">
            <h3>Pets do cliente</h3>

            <c:forEach var="p" items="${pets}">
                <div class="linha-pet">
                    <form action="/admin/clientes/pet/salvar" method="POST">
                        <input type="hidden" name="cpf" value="${cliente.cpf}">
                        <input type="hidden" name="codigoPet" value="${p.id}">
                        <div class="grid2">
                            <div class="form-group">
                                <label>Nome do pet</label>
                                <input type="text" name="nome" value="${p.nome}" required>
                            </div>
                            <div class="form-group">
                                <label>Espécie</label>
                                <input type="text" name="especie" value="${p.especie}" placeholder="Cão / Gato">
                            </div>
                        </div>
                        <div class="grid2">
                            <div class="form-group">
                                <label>Raça</label>
                                <input type="text" name="raca" value="${p.raca}" placeholder="Poodle">
                            </div>
                            <div class="form-group">
                                <label>Cor</label>
                                <input type="text" name="cor" placeholder="Branco">
                                <span class="mini">Gravada junto da raça: "Poodle / Branco"</span>
                            </div>
                        </div>
                        <div class="grid2">
                            <div class="form-group">
                                <label>Idade (anos)</label>
                                <input type="number" name="idade" value="${p.idade}" min="0">
                            </div>
                            <div class="form-group">
                                <label>Peso (kg)</label>
                                <input type="number" name="peso" step="0.01" value="${p.peso}" min="0">
                            </div>
                        </div>
                        <button type="submit" class="btn btn-sucesso btn-sm">Salvar pet</button>
                    </form>
                    <form action="/admin/clientes/pet/excluir" method="POST" style="margin-top:0.5rem;"
                          onsubmit="return confirm('Remover o pet ${p.nome}?')">
                        <input type="hidden" name="cpf" value="${cliente.cpf}">
                        <input type="hidden" name="codigoPet" value="${p.id}">
                        <button type="submit" class="btn btn-cancelar btn-sm">Remover pet</button>
                    </form>
                </div>
            </c:forEach>

            <c:if test="${empty pets}">
                <p class="mini">Nenhum pet cadastrado para este cliente.</p>
            </c:if>

            <h4 style="margin-top:1.4rem;">➕ Adicionar novo pet</h4>
            <form action="/admin/clientes/pet/salvar" method="POST">
                <input type="hidden" name="cpf" value="${cliente.cpf}">
                <div class="grid2">
                    <div class="form-group">
                        <label>Nome do pet</label>
                        <input type="text" name="nome" required placeholder="Ex: Bob">
                    </div>
                    <div class="form-group">
                        <label>Espécie</label>
                        <input type="text" name="especie" placeholder="Cão / Gato">
                    </div>
                </div>
                <div class="grid2">
                    <div class="form-group">
                        <label>Raça</label>
                        <input type="text" name="raca" placeholder="Poodle">
                    </div>
                    <div class="form-group">
                        <label>Cor</label>
                        <input type="text" name="cor" placeholder="Branco">
                    </div>
                </div>
                <div class="grid2">
                    <div class="form-group">
                        <label>Idade (anos)</label>
                        <input type="number" name="idade" min="0">
                    </div>
                    <div class="form-group">
                        <label>Peso (kg)</label>
                        <input type="number" name="peso" step="0.01" min="0">
                    </div>
                </div>
                <button type="submit" class="btn btn-sucesso" style="padding:0.7rem 1.4rem;">Adicionar pet</button>
            </form>
        </div>

        <!-- ============ PARENTES ============ -->
        <div class="secao">
            <h3>CPFs de parentes vinculados</h3>
            <p class="mini" style="margin-bottom:0.8rem;">
                Usado quando o pet é trazido por outra pessoa da família.
            </p>

            <div style="margin-bottom:1rem;">
                <c:forEach var="par" items="${parentes}">
                    <span class="chip">
                        ${par}
                        <form action="/admin/clientes/parente/remover" method="POST" style="display:inline;">
                            <input type="hidden" name="cpf" value="${cliente.cpf}">
                            <input type="hidden" name="cpfParente" value="${par}">
                            <button type="submit" style="border:none;background:none;color:#c62828;cursor:pointer;font-weight:bold;">×</button>
                        </form>
                    </span>
                </c:forEach>
                <c:if test="${empty parentes}">
                    <span class="mini">Nenhum parente vinculado.</span>
                </c:if>
            </div>

            <form action="/admin/clientes/parente/adicionar" method="POST" style="display:flex; gap:0.6rem; align-items:flex-end;">
                <input type="hidden" name="cpf" value="${cliente.cpf}">
                <div class="form-group" style="flex:1; margin:0;">
                    <label>CPF do parente</label>
                    <input type="text" name="cpfParente" required placeholder="000.000.000-00">
                </div>
                <button type="submit" class="btn btn-sucesso" style="padding:0.7rem 1.4rem;">Vincular</button>
            </form>
        </div>
    </main>
</body>
</html>
