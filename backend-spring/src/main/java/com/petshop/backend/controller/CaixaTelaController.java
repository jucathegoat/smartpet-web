package com.petshop.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.petshop.backend.model.Caixa;
import com.petshop.backend.model.Despesa;
import com.petshop.backend.model.Receita;
import com.petshop.backend.repository.CaixaRepository;
import com.petshop.backend.repository.DespesaRepository;
import com.petshop.backend.repository.ReceitaRepository;

/**
 * Tela de fluxo de caixa servida diretamente como HTML.
 * (O JSP nao estava sendo resolvido pelo Tomcat embutido, entao a pagina
 *  e gerada aqui mesmo - sem dependencia de view resolver.)
 */
@Controller
@CrossOrigin(origins = "*")
public class CaixaTelaController {

    private final CaixaRepository caixaRepository;
    private final ReceitaRepository receitaRepository;
    private final DespesaRepository despesaRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_CURTO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public CaixaTelaController(CaixaRepository caixaRepository,
                               ReceitaRepository receitaRepository,
                               DespesaRepository despesaRepository) {
        this.caixaRepository = caixaRepository;
        this.receitaRepository = receitaRepository;
        this.despesaRepository = despesaRepository;
    }

    // ==========================================================
    // TELA
    // ==========================================================

    @GetMapping(value = "/caixa", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String tela(jakarta.servlet.http.HttpSession session) {

        if (session.getAttribute("adminLogado") == null) {
            return "<meta http-equiv='refresh' content='0;url=/login'>";
        }

        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();
        Caixa caixa = opt.orElse(null);
        boolean aberto = caixa != null;

        BigDecimal saldoInicial = BigDecimal.ZERO;
        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal saidas = BigDecimal.ZERO;

        StringBuilder linhasEntrada = new StringBuilder();
        StringBuilder linhasSaida = new StringBuilder();

        if (aberto) {
            saldoInicial = nz(caixa.getSaldoInicial());

            for (Receita r : receitaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
                entradas = entradas.add(nz(r.getValor()));
                linhasEntrada.append(linha(
                        r.getDataHora() != null ? r.getDataHora().format(FMT_CURTO) : "-",
                        r.getCategoria(), r.getDescricao(), nz(r.getValor()), true,
                        "entrada", String.valueOf(r.getIdReceita())));
            }

            for (Despesa d : despesaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
                saidas = saidas.add(nz(d.getValor()));
                linhasSaida.append(linha(
                        d.getDataHora() != null ? d.getDataHora().format(FMT_CURTO) : "-",
                        d.getCategoria(), d.getDescricao(), nz(d.getValor()), false,
                        "saida", String.valueOf(d.getIdDespesa())));
            }
        }

        BigDecimal saldoAtual = saldoInicial.add(entradas).subtract(saidas);

        if (linhasEntrada.length() == 0) {
            linhasEntrada.append("<tr><td colspan='5' class='vazio'>Nenhuma entrada lançada.</td></tr>");
        }
        if (linhasSaida.length() == 0) {
            linhasSaida.append("<tr><td colspan='5' class='vazio'>Nenhuma saída lançada.</td></tr>");
        }

        StringBuilder hist = new StringBuilder();
        for (Caixa c : caixaRepository.findAllByOrderByIdCaixaDesc()) {
            boolean fechado = c.getHorarioFechamento() != null;
            hist.append("<tr>")
                .append("<td>").append(c.getIdCaixa()).append("</td>")
                .append("<td>").append(c.getHorarioAbertura() != null ? c.getHorarioAbertura().format(FMT) : "-").append("</td>")
                .append("<td>").append(fechado ? c.getHorarioFechamento().format(FMT) : "—").append("</td>")
                .append("<td>").append(moeda(nz(c.getSaldoInicial()))).append("</td>")
                .append("<td>").append(c.getSaldoFinal() != null ? moeda(c.getSaldoFinal()) : "—").append("</td>")
                .append("<td>").append(fechado ? "<span style='color:#777'>Fechado</span>"
                                               : "<span style='color:#2e7d32;font-weight:700'>Aberto</span>").append("</td>")
                .append("</tr>");
        }
        if (hist.length() == 0) {
            hist.append("<tr><td colspan='6' class='vazio'>Nenhum caixa aberto ainda.</td></tr>");
        }

        String blocoAbertura = aberto
            ? "<form action='/caixa/fechar' method='POST' onsubmit=\"return confirm('Fechar o caixa com saldo de R$ " + moeda(saldoAtual) + "?')\">"
              + "<button type='submit' class='btn vermelho'>🔒 Fechar caixa</button></form>"
            : "<form action='/caixa/abrir' method='POST' class='linha'>"
              + "<div class='campo'><label>Saldo inicial (R$)</label>"
              + "<input type='text' name='saldoInicial' value='0,00' style='width:130px'></div>"
              + "<div class='campo'><label>Forma de pagamento</label>"
              + "<select name='formaPagamento'><option>Dinheiro</option><option>Pix</option><option>Cartão</option></select></div>"
              + "<button type='submit' class='btn verde'>🔓 Abrir caixa</button></form>";

        String aviso = aberto ? "" :
            "<div class='aviso'>⚠️ O caixa está fechado. Abra o caixa para começar a registrar entradas e saídas.</div>";

        String blocoMovimento = aberto
            ? "<div class='cartao'><h3 style='margin-top:0'>➕ Lançar movimento</h3>"
              + "<form action='/caixa/movimento' method='POST' class='linha'>"
              + "<div class='campo'><label>Tipo</label><select name='tipo'>"
              + "<option value='entrada'>Entrada (entrou)</option><option value='saida'>Saída (saiu)</option></select></div>"
              + "<div class='campo'><label>Categoria</label><input name='categoria' list='cats' placeholder='Banho, Ração, Luz...' style='width:170px'></div>"
              + "<div class='campo'><label>Descrição</label><input name='descricao' placeholder='Ex: Banho da Mel' style='width:210px'></div>"
              + "<div class='campo'><label>Valor (R$)</label><input name='valor' placeholder='0,00' required style='width:110px'></div>"
              + "<button type='submit' class='btn verde'>Registrar</button></form>"
              + "<datalist id='cats'>"
              + "<option value='Banho e Tosa'><option value='Consulta Veterinária'><option value='Vacinação'>"
              + "<option value='Venda de Produto'><option value='Ração'><option value='Aluguel'>"
              + "<option value='Energia'><option value='Salário'><option value='Licença'>"
              + "<option value='Material de Limpeza'></datalist></div>"
            : "";

        String blocoTabelas = aberto
            ? "<div class='duas'>"
              + "<div><h3>Entradas</h3><table><thead><tr><th>Data</th><th>Categoria</th><th>Descrição</th><th>Valor</th><th></th></tr></thead>"
              + "<tbody>" + linhasEntrada + "</tbody></table></div>"
              + "<div><h3>Saídas</h3><table><thead><tr><th>Data</th><th>Categoria</th><th>Descrição</th><th>Valor</th><th></th></tr></thead>"
              + "<tbody>" + linhasSaida + "</tbody></table></div>"
              + "</div>"
            : "";

        return "<!DOCTYPE html><html lang='pt-br'><head><meta charset='UTF-8'>"
             + "<title>Caixa - Smart Pet</title>"
             + "<style>" + CSS + "</style></head><body>"
             + "<header class='nav'><div class='logo'>🐾 Smart Pet Admin</div><nav>"
             + "<a href='/fila'>Fila</a><a href='/pets'>Pets</a><a href='/clientes'>Clientes</a>"
             + "<a href='/loja'>Loja</a><a href='/caixa' class='ativo'>Caixa</a>"
             + "<a href='/avaliacoes'>Avaliações</a><a href='/notificacoes'>Notificações</a>"
             + "<a href='/logout' style='color:#ffcdd2'>Sair</a></nav></header>"
             + "<main class='container'><h2>Fluxo de Caixa</h2>"
             + aviso
             + "<div class='cards'>"
             + card("Saldo inicial", moeda(saldoInicial), "cinza")
             + card("Entradas", "+ " + moeda(entradas), "verde")
             + card("Saídas", "− " + moeda(saidas), "vermelho")
             + card("Saldo atual", moeda(saldoAtual), "azul")
             + "</div>"
             + "<div style='margin-bottom:1.4rem'>" + blocoAbertura + "</div>"
             + blocoMovimento
             + blocoTabelas
             + "<h3 style='margin-top:2.5rem'>Histórico de caixas</h3>"
             + "<table><thead><tr><th>#</th><th>Abertura</th><th>Fechamento</th><th>Saldo inicial</th><th>Saldo final</th><th>Status</th></tr></thead>"
             + "<tbody>" + hist + "</tbody></table>"
             + "</main></body></html>";
    }

    // ==========================================================
    // ACOES
    // ==========================================================

    @PostMapping("/caixa/abrir")
    public String abrir(@RequestParam(value = "saldoInicial", required = false) String saldoInicial,
                        @RequestParam(value = "formaPagamento", required = false) String formaPagamento,
                        jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("adminLogado") == null) return "redirect:/login";
        if (caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc().isPresent()) {
            return "redirect:/caixa";
        }
        Caixa c = new Caixa(parse(saldoInicial), LocalDateTime.now());
        c.setFormaPagamento(formaPagamento);
        caixaRepository.save(c);
        return "redirect:/caixa";
    }

    @PostMapping("/caixa/fechar")
    public String fechar(jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("adminLogado") == null) return "redirect:/login";

        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();
        if (opt.isEmpty()) return "redirect:/caixa";

        Caixa caixa = opt.get();
        BigDecimal entradas = BigDecimal.ZERO;
        for (Receita r : receitaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            entradas = entradas.add(nz(r.getValor()));
        }
        BigDecimal saidas = BigDecimal.ZERO;
        for (Despesa d : despesaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            saidas = saidas.add(nz(d.getValor()));
        }

        caixa.setSaldoFinal(nz(caixa.getSaldoInicial()).add(entradas).subtract(saidas));
        caixa.setHorarioFechamento(LocalDateTime.now());
        caixaRepository.save(caixa);
        return "redirect:/caixa";
    }

    @PostMapping("/caixa/movimento")
    public String lancar(@RequestParam("tipo") String tipo,
                         @RequestParam(value = "categoria", required = false) String categoria,
                         @RequestParam(value = "descricao", required = false) String descricao,
                         @RequestParam("valor") String valor,
                         jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("adminLogado") == null) return "redirect:/login";

        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();
        if (opt.isEmpty()) return "redirect:/caixa";

        Integer idCaixa = opt.get().getIdCaixa();
        BigDecimal v = parse(valor);

        if ("saida".equalsIgnoreCase(tipo)) {
            despesaRepository.save(new Despesa(categoria, descricao, v, LocalDateTime.now(), idCaixa));
        } else {
            receitaRepository.save(new Receita(categoria, descricao, v, LocalDateTime.now(), idCaixa));
        }
        return "redirect:/caixa";
    }

    @PostMapping("/caixa/movimento/excluir")
    public String excluir(@RequestParam("tipo") String tipo,
                          @RequestParam("id") Integer id,
                          jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("adminLogado") == null) return "redirect:/login";

        if ("saida".equalsIgnoreCase(tipo)) {
            despesaRepository.deleteById(id);
        } else {
            receitaRepository.deleteById(id);
        }
        return "redirect:/caixa";
    }

    // ==========================================================
    // HTML helpers
    // ==========================================================

    private static String linha(String data, String categoria, String descricao,
                                BigDecimal valor, boolean entrada, String tipo, String id) {
        return "<tr><td>" + esc(data) + "</td><td>" + esc(categoria) + "</td><td>" + esc(descricao) + "</td>"
             + "<td class='" + (entrada ? "pos" : "neg") + "'>" + (entrada ? "+ " : "− ") + moeda(valor) + "</td>"
             + "<td><form action='/caixa/movimento/excluir' method='POST' style='display:inline'"
             + " onsubmit=\"return confirm('Excluir este lançamento?')\">"
             + "<input type='hidden' name='tipo' value='" + tipo + "'>"
             + "<input type='hidden' name='id' value='" + id + "'>"
             + "<button type='submit' class='x'>✕</button></form></td></tr>";
    }

    private static String card(String rotulo, String valor, String cor) {
        return "<div class='cartao-saldo " + cor + "'><div class='rot'>" + rotulo + "</div>"
             + "<div class='val'>" + valor + "</div></div>";
    }

    /** Formata 255.00 como "255,00". */
    private static String moeda(BigDecimal v) {
        if (v == null) return "0,00";
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "R$ " + nf.format(v);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal parse(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String limpo = s.trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
        if (limpo.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(limpo); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private static final String CSS =
        "*{box-sizing:border-box}"
        + "body{margin:0;font-family:-apple-system,Segoe UI,Roboto,sans-serif;background:#f5f6f8;color:#263238}"
        + ".nav{background:#1b5e20;color:#fff;padding:.9rem 1.6rem;display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:1rem}"
        + ".nav .logo{font-weight:700;font-size:1.1rem}"
        + ".nav nav a{color:#e8f5e9;text-decoration:none;margin-right:1.1rem;font-weight:500;font-size:.92rem}"
        + ".nav nav a:hover{text-decoration:underline}"
        + ".nav nav a.ativo{font-weight:800;text-decoration:underline;color:#fff}"
        + ".container{max-width:1180px;margin:0 auto;padding:1.8rem 1.4rem 3rem}"
        + "h2{margin:.2rem 0 1.4rem;font-size:1.6rem}h3{font-size:1.05rem;margin:1.2rem 0 .6rem}"
        + ".cards{display:flex;gap:1rem;flex-wrap:wrap;margin-bottom:1.5rem}"
        + ".cartao-saldo{flex:1 1 190px;background:#fff;border-radius:10px;padding:1.1rem 1.2rem;box-shadow:0 2px 8px rgba(0,0,0,.07);border-left:5px solid #ccc}"
        + ".cartao-saldo .rot{font-size:.72rem;letter-spacing:.6px;text-transform:uppercase;color:#78909c;font-weight:700}"
        + ".cartao-saldo .val{font-size:1.5rem;font-weight:800;margin-top:.35rem}"
        + ".cartao-saldo.verde{border-left-color:#2e7d32}.cartao-saldo.verde .val{color:#2e7d32}"
        + ".cartao-saldo.vermelho{border-left-color:#c62828}.cartao-saldo.vermelho .val{color:#c62828}"
        + ".cartao-saldo.azul{border-left-color:#1565c0}.cartao-saldo.azul .val{color:#1565c0}"
        + ".cartao-saldo.cinza{border-left-color:#616161}.cartao-saldo.cinza .val{color:#424242}"
        + ".aviso{background:#fff8e1;border:1px solid #ffe082;color:#795548;padding:1rem 1.2rem;border-radius:8px;margin-bottom:1.4rem;font-weight:600}"
        + ".cartao{background:#fff;border-radius:10px;padding:1.2rem;box-shadow:0 2px 8px rgba(0,0,0,.07);margin-bottom:1.5rem}"
        + ".linha{display:flex;gap:.7rem;flex-wrap:wrap;align-items:flex-end}"
        + ".campo{display:flex;flex-direction:column;gap:.25rem}"
        + ".campo label{font-size:.75rem;color:#607d8b;font-weight:700;text-transform:uppercase;letter-spacing:.4px}"
        + ".campo input,.campo select{padding:.55rem .7rem;border:1px solid #cfd8dc;border-radius:6px;font-size:.95rem;background:#fff}"
        + ".btn{border:0;padding:.62rem 1.2rem;border-radius:6px;font-weight:700;cursor:pointer;color:#fff;font-size:.92rem}"
        + ".btn.verde{background:#2e7d32}.btn.vermelho{background:#c62828}.btn:hover{filter:brightness(1.08)}"
        + "table{width:100%;border-collapse:collapse;background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.07)}"
        + "thead{background:#eceff1}th,td{padding:.65rem .8rem;text-align:left;font-size:.9rem;border-bottom:1px solid #eceff1}"
        + "th{font-size:.75rem;text-transform:uppercase;letter-spacing:.5px;color:#546e7a}"
        + ".pos{color:#2e7d32;font-weight:700}.neg{color:#c62828;font-weight:700}"
        + ".vazio{text-align:center;color:#90a4ae;padding:1.4rem}"
        + ".duas{display:flex;gap:1.5rem;flex-wrap:wrap}.duas>div{flex:1 1 400px}"
        + ".x{background:none;border:0;color:#c62828;cursor:pointer;font-size:1rem}";
}
