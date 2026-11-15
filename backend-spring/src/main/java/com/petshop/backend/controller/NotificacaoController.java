package com.petshop.backend.controller;

import com.petshop.backend.model.Notificacao;
import com.petshop.backend.repository.NotificacaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoController(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping("/notificacoes")
    public String listarNotificacoesWeb(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("notificacoes", notificacaoRepository.findAll());
        return "notificacoes";
    }

    @PostMapping("/admin/notificacoes/enviar")
    public String enviarNotificacaoWeb(@RequestParam("titulo") String titulo,
                                       @RequestParam("mensagem") String mensagem,
                                       HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        Notificacao notif = new Notificacao(titulo, mensagem, LocalDateTime.now());
        notificacaoRepository.save(notif);
        return "redirect:/notificacoes";
    }

    @PostMapping("/admin/notificacoes/excluir")
    public String excluirNotificacaoWeb(@RequestParam("id") Integer id, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        notificacaoRepository.deleteById(id);
        return "redirect:/notificacoes";
    }

    // ==========================================================
    // APIS REST (CONSUMIDAS PELO FLUTTER)
    // ==========================================================

    @GetMapping("/api/notificacoes")
    @ResponseBody
    public ResponseEntity<List<Notificacao>> listarNotificacoesApi() {
        return ResponseEntity.ok(notificacaoRepository.findAll());
    }
}