<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Balcão - Smart Pet</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .secao { background:#fff; border:1px solid #e0e0e0; border-radius:10px; padding:1.4rem; margin-bottom:1.4rem; }
        .secao h3 { margin-top:0; color:#333; border-bottom:2px solid #e8e8e8; padding-bottom:0.6rem; }
        .grid3 { display:grid; grid-template-columns: 2fr 2fr auto; gap:1rem; align-items:end; }
        .perfil { display:inline-block; background:#e3f2fd; color:#1565c0; padding:0.3rem 0.9rem;
                  border-radius:16px; font-size:0.8rem; font-weight:600; margin-bottom:1rem; }
        .vazio { text-align:center; color:#999; padding:2rem; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="logo">🐾 Smart Pet — Balcão</div>
        <nav>
            <a href="/balconista">Balcão</a>
            <a href="/veterinario">Veterinário</a>
            <a href="/fila">Fila completa</a>
            <a href="/clientes">Clientes</a>
            <a href="/logout" style="color:#ffcdd2;">Sair</a>
        </nav>
    </header>
    <main class="container">
        <span class="perfil">👤 Perfil: Balconista / Atendente</span>
        <h2>Recepção e Fila de Espera</h2>
        <p style="color:#666; margin-bottom:1.4rem;">
            Registre a chegada do pet e chame o próximo para o veterinário.
        </p>

        <c:if test="${not empty mensagem}">
            <div style="background:#e8f5e9;border:1px solid #a5d6a7;color:#2e7d32;padding:1rem 1.2rem;border-radius:8px;margin-bottom:1.4rem;font-weight:600;">
                ✅ ${mensagem}
            </div>
        </c:if>
        <c:if test="${not empty erro}">
            <div style="background:#ffebee;border:1px solid #ef9a9a;color:#c62828;padding:1rem 1.2rem;border-radius:8px;margin-bottom:1.4rem;font-weight:600;">
                ⚠️ ${erro}
            </div>
        </c:if>

        <!-- REGISTRAR CHEGADA -->
        <div class="secao">
            <h3>Registrar chegada</h3>
            <form action="/balconista/registrar" method="POST">
                <div class="grid3">
                    <div class="form-group" style="margin:0;">
                        <label>Pet (lista do banco)</label>
                        <select name="idPet" required>
                            <option value="">Selecione o pet...</option>
                            <c:forEach var="p" items="${pets}">
                                <option value="${p.id}">${p.nome} — ${p.especie}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group" style="margin:0;">
                        <label>Serviço (lista do banco)</label>
                        <select name="idServico" required>
                            <option value="">Selecione o serviço...</option>
                            <c:forEach var="s" items="${servicos}">
                                <option value="${s.idServico}">${s.nome} — R$ ${s.preco}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-sucesso" style="padding:0.75rem 1.6rem;">Adicionar à fila</button>
                </div>
            </form>
            <p style="font-size:0.78rem; color:#999; margin-top:0.7rem;">
                As opções vêm das tabelas <code>pet</code> e <code>servico</code> do banco.
            </p>
        </div>

        <!-- FILA DE ESPERA -->
        <div class="secao">
            <h3>Fila de espera — quem está aguardando</h3>
            <table class="tabela">
                <thead>
                    <tr>
                        <th>Pos.</th>
                        <th>Cliente</th>
                        <th>Pet</th>
                        <th>Serviço</th>
                        <th>Chegada</th>
                        <th>Espera</th>
                        <th>Status</th>
                        <th>Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="pos" value="0" />
                    <c:forEach var="item" items="${fila}">
                        <tr>
                            <td>
                                <c:choose>
                                    <c:when test="${item.status == 'Em espera'}">
                                        <c:set var="pos" value="${pos + 1}" />
                                        <strong>${pos}º</strong>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td>${item.nomeCliente}</td>
                            <td><strong>${item.nomePet}</strong></td>
                            <td>${item.servico}</td>
                            <td>${item.horarioChegada}</td>
                            <td>${item.minutosEspera} min</td>
                            <td><span class="badge ${item.statusClass}">${item.status}</span></td>
                            <td>
                                <c:if test="${item.status == 'Em espera'}">
                                    <form action="/fila/acao" method="POST" style="display:inline;">
                                        <input type="hidden" name="acao" value="chamar">
                                        <button type="submit" class="btn btn-sucesso btn-sm">Chamar próximo</button>
                                    </form>
                                </c:if>
                                <button type="button" class="btn btn-cancelar btn-sm"
                                        onclick="abrirAcoes(${item.id}, '${item.nomePet}', '${item.status}')">Ações</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty fila}">
                        <tr><td colspan="8" class="vazio">Ninguém na fila agora.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </main>

    <!-- ============ MODAL DE AÇÕES (não exclui, só muda status) ============ -->
    <div id="modalAcoes" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.5); z-index:1000; align-items:center; justify-content:center;">
        <div style="background:#fff; border-radius:12px; padding:1.8rem; width:min(460px,92vw); box-shadow:0 10px 40px rgba(0,0,0,0.3);">
            <h3 id="modalTitulo" style="margin-top:0;">Ações do atendimento</h3>
            <p id="modalStatus" style="color:#666; font-size:0.9rem;"></p>

            <form action="/fila/acao" method="POST" id="formConcluir" style="margin-bottom:0.8rem;">
                <input type="hidden" name="acao" value="concluir">
                <input type="hidden" name="id" id="idConcluir">
                <button type="submit" class="btn btn-sucesso" style="width:100%; padding:0.8rem;">
                    ✅ Concluir atendimento
                </button>
            </form>

            <form action="/fila/acao" method="POST" id="formCancelar">
                <input type="hidden" name="acao" value="cancelar">
                <input type="hidden" name="id" id="idCancelar">
                <div class="form-group">
                    <label>Motivo do cancelamento (obrigatório)</label>
                    <input type="text" name="motivo" required placeholder="Ex: cliente desistiu">
                </div>
                <button type="submit" class="btn btn-cancelar" style="width:100%; padding:0.8rem;">
                    ✖ Cancelar atendimento
                </button>
            </form>

            <p style="font-size:0.75rem; color:#999; margin-top:1rem; text-align:center;">
                Nada é apagado do banco: o histórico é preservado com o novo status.
            </p>
            <button type="button" onclick="fecharAcoes()"
                    style="width:100%; margin-top:0.6rem; background:none; border:none; color:#666; cursor:pointer; font-size:0.9rem;">
                Voltar
            </button>
        </div>
    </div>

    <script>
        function abrirAcoes(id, nomePet, status) {
            document.getElementById('modalTitulo').textContent = 'Ações — ' + nomePet;
            document.getElementById('modalStatus').textContent = 'Status atual: ' + status;
            document.getElementById('idConcluir').value = id;
            document.getElementById('idCancelar').value = id;
            document.getElementById('modalAcoes').style.display = 'flex';
        }
        function fecharAcoes() {
            document.getElementById('modalAcoes').style.display = 'none';
        }
        document.getElementById('modalAcoes').addEventListener('click', function (e) {
            if (e.target === this) fecharAcoes();
        });
    </script>
</body>
</html>
