package com.petshop.backend.controller;

import com.petshop.backend.model.Produto;
import com.petshop.backend.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                  @RequestParam(value = "url", required = false) String url,
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
        prod.setUrl(url != null ? url.trim() : null);
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

    // ==========================================================
    // API: cadastrar produto pelo app, com foto
    // ==========================================================

    @PostMapping("/api/produtos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cadastrarProdutoApi(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> resp = new HashMap<>();

        if (body.get("nome") == null || body.get("nome").toString().isBlank()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "O nome do produto é obrigatório.");
            return ResponseEntity.badRequest().body(resp);
        }

        Produto p = new Produto();
        p.setNome(body.get("nome").toString());
        p.setMarca(body.get("marca") != null ? body.get("marca").toString() : "Geral");
        p.setCategoria(body.get("categoria") != null ? body.get("categoria").toString() : "Geral");
        p.setEstoque(body.get("estoque") != null
                ? Integer.valueOf(body.get("estoque").toString()) : 0);
        p.setPreco(body.get("preco") != null
                ? new BigDecimal(body.get("preco").toString()) : BigDecimal.ZERO);
        if (body.get("url") != null && !body.get("url").toString().isBlank()) {
            p.setUrl(body.get("url").toString().trim());
        }

        produtoRepository.save(p);

        resp.put("sucesso", true);
        resp.put("mensagem", "Produto cadastrado com foto.");
        resp.put("produto", p);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/produtos/{codigo}")
    @ResponseBody
    public ResponseEntity<?> buscarProdutoApi(@PathVariable("codigo") Integer codigo) {
        return produtoRepository.findById(codigo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "sucesso", false,
                        "mensagem", "Produto não encontrado")));
    }
}