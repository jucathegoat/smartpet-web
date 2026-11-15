package com.petshop.backend.controller;

import com.petshop.backend.model.Produto;
import com.petshop.backend.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping("/loja")
    public String listarProdutosWeb(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("produtos", produtoRepository.findAll());
        return "loja";
    }

    @PostMapping("/admin/produtos/salvar")
    public String salvarProdutoWeb(@RequestParam(value = "codigo", required = false) Integer codigo,
                                  @RequestParam("nome") String nome,
                                  @RequestParam(value = "marca", required = false) String marca,
                                  @RequestParam(value = "categoria", required = false) String categoria,
                                  @RequestParam("estoque") Integer estoque,
                                  @RequestParam("preco") BigDecimal preco,
                                  HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        Produto prod = (codigo != null) ? produtoRepository.findById(codigo).orElse(new Produto()) : new Produto();
        prod.setNome(nome);
        prod.setMarca(marca != null ? marca : "Geral");
        prod.setCategoria(categoria != null ? categoria : "Geral");
        prod.setEstoque(estoque);
        prod.setPreco(preco);
        produtoRepository.save(prod);

        return "redirect:/loja";
    }

    @PostMapping("/admin/produtos/excluir")
    public String excluirProdutoWeb(@RequestParam("codigo") Integer codigo, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        produtoRepository.deleteById(codigo);
        return "redirect:/loja";
    }

    // ==========================================================
    // APIS REST (CONSUMIDAS PELO FLUTTER)
    // ==========================================================

    @GetMapping("/api/produtos")
    @ResponseBody
    public ResponseEntity<List<Produto>> listarProdutosApi() {
        return ResponseEntity.ok(produtoRepository.findAll());
    }
}