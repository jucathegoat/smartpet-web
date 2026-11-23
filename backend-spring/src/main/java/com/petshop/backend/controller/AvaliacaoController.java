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
    private final com.petshop.backend.service.FilaService filaService;
    private final com.petshop.backend.repository.PetRepository petRepository;
    private final com.petshop.backend.repository.TutelaRepository tutelaRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository,
                               com.petshop.backend.service.FilaService filaService,
                               com.petshop.backend.repository.PetRepository petRepository,
                               com.petshop.backend.repository.TutelaRepository tutelaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.filaService = filaService;
        this.petRepository = petRepository;
        this.tutelaRepository = tutelaRepository;
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
    public ResponseEntity<List<Map<String, Object>>> listarPendentesApi(
            @RequestParam(value = "cpf", required = false) String cpf) {
        List<Map<String, Object>> resp = new ArrayList<>();
        Set<Integer> petsDoTutor = (cpf != null && !cpf.isBlank())
                ? new HashSet<>(tutelaRepository.findCodigoPetByCpf(cpf))
                : Collections.emptySet();

        for (com.petshop.backend.model.ItemFilaDTO item : filaService.listarTodos()) {
            if ("Concluído".equalsIgnoreCase(item.getStatus()) || "Concluido".equalsIgnoreCase(item.getStatus())) {
                if (cpf != null && !cpf.isBlank()) {
                    // só exibe se o tutor for o dono do pet
                    if (item.getIdPet() == null || !petsDoTutor.contains(item.getIdPet())) continue;
                }

                boolean jaRespondida = avaliacaoRepository.findByServicoId(item.getId())
                        .map(Avaliacao::getRespondida).orElse(false);
                if (!jaRespondida) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getId());
                    map.put("nome_pet", item.getNomePet());
                    map.put("pet_id", item.getIdPet());
                    map.put("descricao", item.getServico());
                    resp.add(map);
                }
            }
        }

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/avaliacoes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarAvaliacaoApi(@RequestBody Map<String, Object> body) {
        Integer servicoId = body.get("servico_id") != null ? Integer.valueOf(body.get("servico_id").toString()) : 1;
        Integer nota;
        try {
            nota = body.get("nota") != null ? Integer.valueOf(body.get("nota").toString()) : null;
        } catch (NumberFormatException e) {
            nota = null;
        }

        // RNF03 — a nota aceita exclusivamente 1, 2 ou 3
        if (nota == null || nota < 1 || nota > 3) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("mensagem", "A nota deve ser 1, 2 ou 3.");
            return ResponseEntity.badRequest().body(erro);
        }

        String comentario = (String) body.getOrDefault("comentario", "");

        Optional<Avaliacao> opt = avaliacaoRepository.findByServicoId(servicoId);
        Avaliacao aval = opt.orElseGet(Avaliacao::new);
        aval.setServicoId(servicoId);
        aval.setNota(nota);
        aval.setComentario(comentario);
        aval.setAvalData(LocalDateTime.now());
        aval.setRespondida(true);
        if (body.get("nome_pet") != null) {
            aval.setNomePet(body.get("nome_pet").toString());
        } else if (aval.getNomePet() == null) {
            aval.setNomePet("Pet");
        }
        if (body.get("descricao") != null) {
            aval.setDescricao(body.get("descricao").toString());
        } else if (aval.getDescricao() == null) {
            aval.setDescricao("Atendimento");
        }

        avaliacaoRepository.save(aval);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("mensagem", "Avaliação enviada com sucesso!");
        return ResponseEntity.ok(resp);
    }
}
