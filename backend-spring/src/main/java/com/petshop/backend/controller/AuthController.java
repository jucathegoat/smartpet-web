package com.petshop.backend.controller;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.repository.ClienteRepository;
import com.petshop.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final ClienteRepository clienteRepository;

    public AuthController(AuthService authService, ClienteRepository clienteRepository) {
        this.authService = authService;
        this.clienteRepository = clienteRepository;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("adminLogado") != null) {
            return "redirect:/fila";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("usuario") String usuario,
                               @RequestParam("senha") String senha,
                               HttpSession session,
                               Model model) {
        if (authService.validarAdmin(usuario, senha)) {
            session.setAttribute("adminLogado", usuario);
            return "redirect:/fila";
        }
        model.addAttribute("erro", "Credenciais de administrador inválidas!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin/usuarios/novo")
    public String novoUsuarioWeb(HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        return "cadastro_usuario";
    }

    @PostMapping("/admin/usuarios/novo")
    public String salvarUsuarioWeb(@RequestParam("nome") String nome,
                                  @RequestParam("cpf") String cpf,
                                  @RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "telefone", required = false) String telefone,
                                  @RequestParam(value = "endereco", required = false) String endereco,
                                  @RequestParam("usuario") String usuario,
                                  @RequestParam("senha") String senha,
                                  HttpSession session,
                                  Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        if (clienteRepository.existsById(cpf)) {
            model.addAttribute("erro", "Já existe um cliente cadastrado com este CPF.");
            return "cadastro_usuario";
        }

        Cliente cliente = new Cliente(cpf, nome, email, telefone, endereco, usuario, senha);
        clienteRepository.save(cliente);
        return "redirect:/clientes";
    }

    // ==========================================================
    // APIS REST (CONSUMIDAS PELO FLUTTER)
    // ==========================================================

    @PostMapping(value = {"/login/usuario", "/api/login"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUsuarioApi(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario");
        String senha = body.get("senha");

        Optional<Cliente> opt = authService.autenticarUsuario(usuario, senha);
        Map<String, Object> resp = new HashMap<>();

        if (opt.isPresent()) {
            Cliente c = opt.get();
            resp.put("sucesso", true);
            resp.put("cpf", c.getCpf());
            resp.put("nome", c.getNome());
            resp.put("usuario", c.getUsuario());
            return ResponseEntity.ok(resp);
        } else {
            resp.put("sucesso", false);
            resp.put("mensagem", "Usuário ou senha incorretos.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    @PostMapping(value = {"/usuario", "/api/usuario"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cadastroUsuarioApi(@RequestBody Map<String, String> body) {
        Map<String, Object> resp = new HashMap<>();
        String cpf = body.get("cpf");
        String nome = body.get("nome");
        String email = body.get("email");
        String telefone = body.get("telefone");
        String endereco = body.get("endereco");
        String usuario = body.get("usuario");
        String senha = body.get("senha");

        if (cpf == null || cpf.trim().isEmpty() || usuario == null || usuario.trim().isEmpty()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "CPF e Usuário são obrigatórios.");
            return ResponseEntity.badRequest().body(resp);
        }

        if (clienteRepository.existsById(cpf)) {
            resp.put("sucesso", false);
            resp.put("mensagem", "CPF já cadastrado.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }

        Cliente novo = new Cliente(cpf, nome, email, telefone, endereco, usuario, senha);
        clienteRepository.save(novo);

        resp.put("sucesso", true);
        resp.put("mensagem", "Cliente cadastrado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}