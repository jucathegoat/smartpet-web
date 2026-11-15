package com.petshop.backend.controller;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.repository.ClienteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin(origins = "*")
public class ClienteAdminController {

    private final ClienteRepository clienteRepository;

    public ClienteAdminController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/clientes")
    public String listarClientes(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("clientes", clienteRepository.findAll());
        return "clientes";
    }

    @PostMapping("/admin/clientes/excluir")
    public String excluirCliente(@RequestParam("cpf") String cpf, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        clienteRepository.deleteById(cpf);
        return "redirect:/clientes";
    }
}