package com.petshop.backend.controller;

import com.petshop.backend.model.ItemFilaDTO;
import com.petshop.backend.service.FilaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin(origins = "*")
public class FilaController {

    private final FilaService filaService;

    public FilaController(FilaService filaService) {
        this.filaService = filaService;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping(value = {"/fila", "/FilaServlet"})
    public String exibirFila(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("fila", filaService.listarTodos());
        return "fila";
    }

    @GetMapping("/admin/agendamentos/novo")
    public String novoAgendamentoWeb(HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        return "cadastro_agendamento";
    }

    @PostMapping("/admin/agendamentos/novo")
    public String salvarAgendamentoWeb(@RequestParam("cliente") String cliente,
                                       @RequestParam("pet") String pet,
                                       @RequestParam("servico") String servico,
                                       HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        filaService.adicionar(cliente, pet, servico);
        return "redirect:/fila";
    }

    @PostMapping(value = {"/fila/acao", "/FilaServlet"})
    public String executarAcaoWeb(@RequestParam("acao") String acao,
                                  @RequestParam(value = "id", required = false) Integer id,
                                  @RequestParam(value = "motivo", required = false) String motivo,
                                  HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        if ("chamar".equalsIgnoreCase(acao)) {
            filaService.chamarProximo();
        } else if ("cancelar".equalsIgnoreCase(acao) && id != null) {
            filaService.cancelar(id, motivo != null ? motivo : "Cancelado pela administração");
        } else if ("concluir".equalsIgnoreCase(acao) && id != null) {
            filaService.concluir(id);
        }
        return "redirect:/fila";
    }

    // ==========================================================
    // APIS REST (CONSUMIDAS PELO FLUTTER)
    // ==========================================================

    @GetMapping("/api/fila")
    @ResponseBody
    public ResponseEntity<List<ItemFilaDTO>> listarFilaApi() {
        return ResponseEntity.ok(filaService.listarTodos());
    }

    @PostMapping("/api/fila/adicionar")
    @ResponseBody
    public ResponseEntity<ItemFilaDTO> adicionarApi(
            @RequestParam(value = "cliente", required = false) String cliente,
            @RequestParam(value = "pet", required = false) String pet,
            @RequestParam(value = "servico", required = false) String servico,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        if (body != null) {
            if (cliente == null) cliente = body.get("cliente");
            if (pet == null) pet = body.get("pet");
            if (servico == null) servico = body.get("servico");
        }

        if (cliente == null || cliente.trim().isEmpty()) cliente = "Cliente App";
        if (pet == null || pet.trim().isEmpty()) pet = "Pet";
        if (servico == null || servico.trim().isEmpty()) servico = "Atendimento Geral";

        ItemFilaDTO item = filaService.adicionar(cliente, pet, servico);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/api/fila/chamar-proximo")
    @ResponseBody
    public ResponseEntity<ItemFilaDTO> chamarProximoApi() {
        return ResponseEntity.ok(filaService.chamarProximo());
    }
}