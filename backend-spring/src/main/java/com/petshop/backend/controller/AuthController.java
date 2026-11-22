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

        // RF04 — validacao previa de CPF: avisa se ja existe ou se esta bloqueado
        Optional<Cliente> existente = clienteRepository.findById(cpf);
        if (existente.isPresent()) {
            if (existente.get().isBloqueado()) {
                model.addAttribute("erro",
                        "CPF bloqueado. Motivo: " + existente.get().getMotivoBloqueio()
                        + ". Procure a administração do Pet Shop.");
            } else {
                model.addAttribute("erro",
                        "Já existe um cliente cadastrado com este CPF ("
                        + existente.get().getNome() + ").");
            }
            model.addAttribute("cpfDigitado", cpf);
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

            // RF03 — cliente bloqueado nao entra no aplicativo
            if (c.isBloqueado()) {
                resp.put("sucesso", false);
                resp.put("bloqueado", true);
                resp.put("mensagem", "Sua conta está bloqueada. "
                        + "Procure a loja para mais informações.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }

            resp.put("sucesso", true);
            resp.put("cpf", c.getCpf());
            resp.put("nome", c.getNome());
            resp.put("usuario", c.getUsuario());
            resp.put("statusConta", c.getStatusConta());
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
        String statusConta = body.get("statusConta");

        if (cpf == null || cpf.trim().isEmpty() || usuario == null || usuario.trim().isEmpty()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "CPF e Usuário são obrigatórios.");
            return ResponseEntity.badRequest().body(resp);
        }

        // RF03/RF04 — CPF bloqueado nao cria conta nova; CPF livre e liberado
        Optional<Cliente> existenteApi = clienteRepository.findById(cpf);
        if (existenteApi.isPresent()) {
            Cliente jaCadastrado = existenteApi.get();
            resp.put("sucesso", false);
            if (jaCadastrado.isBloqueado()) {
                resp.put("bloqueado", true);
                resp.put("mensagem",
                        "CPF bloqueado. Motivo: " + jaCadastrado.getMotivoBloqueio());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }
            resp.put("bloqueado", false);
            resp.put("mensagem", "CPF já cadastrado.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }

        Cliente novo = new Cliente(cpf, nome, email, telefone, endereco, usuario, senha);
        clienteRepository.save(novo);

        resp.put("sucesso", true);
        resp.put("mensagem", "Cliente cadastrado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // ==========================================================
    // RF04 — VALIDACAO PREVIA DE CPF (consultada pelo app)
    // Responde: "liberado", "ja existe" ou "bloqueado"
    // ==========================================================

    @GetMapping("/api/clientes/validar/{cpf}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarCpf(@PathVariable("cpf") String cpf) {
        Map<String, Object> resp = new HashMap<>();
        String limpo = cpf == null ? "" : cpf.replaceAll("[^0-9A-Za-z]", "");

        if (limpo.length() < 11) {
            resp.put("sucesso", false);
            resp.put("situacao", "invalido");
            resp.put("mensagem", "CPF inválido. Digite os 11 números.");
            return ResponseEntity.badRequest().body(resp);
        }

        Optional<Cliente> opt = clienteRepository.findById(cpf);
        if (opt.isEmpty()) {
            // tenta sem pontuacao
            for (Cliente c : clienteRepository.findAll()) {
                if (c.getCpf() != null
                        && c.getCpf().replaceAll("[^0-9A-Za-z]", "").equals(limpo)) {
                    opt = Optional.of(c);
                    break;
                }
            }
        }

        if (opt.isEmpty()) {
            resp.put("sucesso", true);
            resp.put("situacao", "liberado");
            resp.put("mensagem", "CPF livre. O cadastro pode continuar.");
            return ResponseEntity.ok(resp);
        }

        Cliente c = opt.get();
        if (c.isBloqueado()) {
            resp.put("sucesso", false);
            resp.put("situacao", "bloqueado");
            resp.put("nome", c.getNome());
            resp.put("motivo", c.getMotivoBloqueio());
            resp.put("mensagem", "CPF bloqueado. Motivo: " + c.getMotivoBloqueio());
            return ResponseEntity.ok(resp);
        }

        resp.put("sucesso", false);
        resp.put("situacao", "existe");
        resp.put("nome", c.getNome());
        resp.put("mensagem", "Já existe um cliente com este CPF (" + c.getNome() + ").");
        return ResponseEntity.ok(resp);
    }
}