package com.petshop.backend.controller;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.repository.ClienteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de clientes / tutores.
 *
 * <p>RF01 — consultar cliente por CPF (ou por nome), com mensagem
 * "CPF nao encontrado" quando a busca nao retorna nada.</p>
 *
 * <p>RF03 — bloquear e desbloquear cliente. O motivo do bloqueio e
 * obrigatorio e fica gravado no campo status_conta no formato
 * "Bloqueado: motivo".</p>
 */
@Controller
@CrossOrigin(origins = "*")
public class ClienteAdminController {

    private final ClienteRepository clienteRepository;

    public ClienteAdminController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // ==========================================================
    // LISTAGEM + BUSCA (RF01)
    // ==========================================================

    @GetMapping("/clientes")
    public String listarClientes(
            @RequestParam(value = "busca", required = false) String busca,
            HttpSession session,
            Model model) {

        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        List<Cliente> clientes;

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim();
            // ArrayList exigido pelo RNF04
            clientes = new ArrayList<>();

            // CPF: busca parcial, para funcionar com ou sem pontuacao
            clientes.addAll(clienteRepository.findByCpfContaining(termo));

            // Nome: so acrescenta quem ainda nao esta na lista
            for (Cliente c : clienteRepository.findByNomeContainingIgnoreCase(termo)) {
                boolean jaTem = false;
                for (Cliente existente : clientes) {
                    if (existente.getCpf().equals(c.getCpf())) {
                        jaTem = true;
                        break;
                    }
                }
                if (!jaTem) {
                    clientes.add(c);
                }
            }

            if (clientes.isEmpty()) {
                model.addAttribute("erro",
                        "CPF não encontrado. Nenhum cliente corresponde a \"" + termo + "\".");
            }
            model.addAttribute("busca", termo);
        } else {
            clientes = clienteRepository.findAll();
        }

        model.addAttribute("clientes", clientes);
        return "clientes";
    }

    // ==========================================================
    // RF03 — BLOQUEAR / DESBLOQUEAR
    // ==========================================================

    @PostMapping("/admin/clientes/bloquear")
    public String bloquearCliente(@RequestParam("cpf") String cpf,
                                  @RequestParam(value = "motivo", required = false) String motivo,
                                  HttpSession session,
                                  RedirectAttributes redirect) {

        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        if (motivo == null || motivo.isBlank()) {
            redirect.addFlashAttribute("erro",
                    "O motivo do bloqueio é obrigatório.");
            return "redirect:/clientes";
        }

        Cliente cliente = clienteRepository.findById(cpf).orElse(null);
        if (cliente == null) {
            redirect.addFlashAttribute("erro", "CPF não encontrado.");
            return "redirect:/clientes";
        }

        cliente.setStatusConta("Bloqueado: " + motivo.trim());
        clienteRepository.save(cliente);

        redirect.addFlashAttribute("mensagem",
                "Cliente " + cliente.getNome() + " bloqueado. Motivo: " + motivo.trim());
        return "redirect:/clientes";
    }

    @PostMapping("/admin/clientes/desbloquear")
    public String desbloquearCliente(@RequestParam("cpf") String cpf,
                                     HttpSession session,
                                     RedirectAttributes redirect) {

        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        Cliente cliente = clienteRepository.findById(cpf).orElse(null);
        if (cliente == null) {
            redirect.addFlashAttribute("erro", "CPF não encontrado.");
            return "redirect:/clientes";
        }

        cliente.setStatusConta("ativo");
        clienteRepository.save(cliente);

        redirect.addFlashAttribute("mensagem",
                "Cliente " + cliente.getNome() + " desbloqueado.");
        return "redirect:/clientes";
    }

    // ==========================================================
    // EXCLUIR
    // ==========================================================

    @PostMapping("/admin/clientes/excluir")
    public String excluirCliente(@RequestParam("cpf") String cpf,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        clienteRepository.deleteById(cpf);
        redirect.addFlashAttribute("mensagem", "Cliente removido.");
        return "redirect:/clientes";
    }

    // ==========================================================
    // API — usada pelo app Flutter
    // ==========================================================

    @GetMapping("/api/clientes/{cpf}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> buscarPorCpfApi(@PathVariable("cpf") String cpf) {
        return clienteRepository.findById(cpf)
                .<org.springframework.http.ResponseEntity<?>>map(org.springframework.http.ResponseEntity::ok)
                .orElseGet(() -> org.springframework.http.ResponseEntity
                        .status(404)
                        .body(java.util.Map.of(
                                "sucesso", false,
                                "mensagem", "CPF não encontrado")));
    }
}
