package com.petshop.backend.controller;

import com.petshop.backend.model.Avaliacao;
import com.petshop.backend.repository.AvaliacaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping("/avaliacoes")
    public String listarAvaliacoesWeb(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("avaliacoes", avaliacaoRepository.findAll());
        return "avaliacoes";
    }

    @PostMapping("/admin/avaliacoes/excluir")
    public String excluirAvaliacaoWeb(@RequestParam("id") Integer id, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        avaliacaoRepository.deleteById(id);
        return "redirect:/avaliacoes";
    }

    // ==========================================================
    // APIS REST (CONSUMIDAS PELO FLUTTER)
    // ==========================================================

    @GetMapping("/api/avaliacoes/pendentes")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarPendentesApi() {
        List<Avaliacao> pendentes = avaliacaoRepository.findByRespondidaFalse();
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Avaliacao a : pendentes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getServicoId() != null ? a.getServicoId() : a.getIdAval());
            map.put("nome_pet", a.getNomePet() != null ? a.getNomePet() : "Pet");
            map.put("descricao", a.getDescricao() != null ? a.getDescricao() : "Atendimento SmartPet");
            resp.add(map);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/avaliacoes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarAvaliacaoApi(@RequestBody Map<String, Object> body) {
        Integer servicoId = body.get("servico_id") != null ? Integer.valueOf(body.get("servico_id").toString()) : 1;
        Integer nota = body.get("nota") != null ? Integer.valueOf(body.get("nota").toString()) : 3;
        String comentario = (String) body.getOrDefault("comentario", "");

        Optional<Avaliacao> opt = avaliacaoRepository.findByServicoId(servicoId);
        Avaliacao aval = opt.orElseGet(Avaliacao::new);
        aval.setServicoId(servicoId);
        aval.setNota(nota);
        aval.setComentario(comentario);
        aval.setAvalData(LocalDateTime.now());
        aval.setRespondida(true);
        if (aval.getNomePet() == null) aval.setNomePet("Pet");
        if (aval.getDescricao() == null) aval.setDescricao("Atendimento");

        avaliacaoRepository.save(aval);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("mensagem", "Avaliação enviada com sucesso!");
        return ResponseEntity.ok(resp);
    }
}