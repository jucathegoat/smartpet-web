package com.petshop.backend.controller;

import com.petshop.backend.model.ItemVenda;
import com.petshop.backend.model.Produto;
import com.petshop.backend.model.Venda;
import com.petshop.backend.repository.ProdutoRepository;
import com.petshop.backend.service.VendaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF06 — Vendas de produtos.
 *
 * <p>Tela /vendas: formulario de venda e historico.
 * A venda entra no caixa aberto como receita (integra com o RF07).</p>
 */
@Controller
@CrossOrigin(origins = "*")
public class VendaController {

    private final VendaService vendaService;
    private final ProdutoRepository produtoRepository;

    public VendaController(VendaService vendaService,
                           ProdutoRepository produtoRepository) {
        this.vendaService = vendaService;
        this.produtoRepository = produtoRepository;
    }

    // ==========================================================
    // TELA
    // ==========================================================

    @GetMapping("/vendas")
    public String exibirVendas(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("vendas", montarHistorico());
        model.addAttribute("totalVendido", vendaService.totalVendido());
        return "vendas";
    }

    @PostMapping("/vendas/registrar")
    public String registrarVendaWeb(@RequestParam("idProduto") Integer idProduto,
                                    @RequestParam("quantidade") Integer quantidade,
                                    @RequestParam(value = "formaPagamento", required = false) String formaPagamento,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        VendaService.ResultadoVenda resultado =
                vendaService.registrarVenda(idProduto, quantidade, formaPagamento);

        redirect.addFlashAttribute(resultado.sucesso ? "mensagem" : "erro",
                resultado.mensagem);
        return "redirect:/vendas";
    }

    // ==========================================================
    // API (FLUTTER)
    // ==========================================================

    @PostMapping("/api/vendas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarVendaApi(
            @RequestBody Map<String, Object> body) {

        String cpf = body.get("cpf") == null ? "" : body.get("cpf").toString().replaceAll("[^0-9]", "");
        if (cpf.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Entre na sua conta antes de comprar."));
        }

        Integer idProduto = body.get("id_produto") != null
                ? Integer.valueOf(body.get("id_produto").toString()) : null;
        Integer quantidade = body.get("quantidade") != null
                ? Integer.valueOf(body.get("quantidade").toString()) : null;
        String formaPagamento = body.get("forma_pagamento") != null
                ? body.get("forma_pagamento").toString() : "Dinheiro";

        VendaService.ResultadoVenda resultado =
                vendaService.registrarVenda(idProduto, quantidade, formaPagamento);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", resultado.sucesso);
        resp.put("mensagem", resultado.mensagem);
        if (resultado.sucesso && resultado.venda != null) {
            resp.put("id_venda", resultado.venda.getIdVenda());
            resp.put("total", resultado.venda.getSubtotal());

        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/vendas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarVendasApi() {
        return ResponseEntity.ok(montarHistorico());
    }

    // ==========================================================
    // Auxiliar: junta venda + item + nome do produto
    // ==========================================================

    private List<Map<String, Object>> montarHistorico() {
        // ArrayList exigido pelo RNF04
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Venda v : vendaService.listarTodas()) {
            Map<String, Object> linha = new HashMap<>();
            linha.put("id", v.getIdVenda());
            linha.put("total", v.getSubtotal());
            linha.put("formaPagamento", v.getFormaPagamento());
            linha.put("dataHora", v.getDataHora() != null ? v.getDataHora().toString() : "");
            linha.put("situacao", v.getSituacao());

            List<ItemVenda> itens = vendaService.itensDaVenda(v.getIdVenda());
            if (!itens.isEmpty()) {
                ItemVenda item = itens.get(0);
                Produto p = produtoRepository.findById(item.getIdProduto()).orElse(null);
                linha.put("produto", p != null ? p.getNome() : "Produto removido");
                linha.put("quantidade", item.getQuantidade());
                linha.put("precoUnitario", item.getPrecoUnitario());
            } else {
                linha.put("produto", "—");
                linha.put("quantidade", 0);
                linha.put("precoUnitario", null);
            }
            lista.add(linha);
        }
        return lista;
    }
}
