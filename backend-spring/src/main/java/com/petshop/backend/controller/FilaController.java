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
    private final com.petshop.backend.repository.PetRepository petRepository;
    private final com.petshop.backend.repository.ClienteRepository clienteRepository;
    private final com.petshop.backend.repository.ServicoRepository servicoRepository;

    public FilaController(FilaService filaService,
                          com.petshop.backend.repository.PetRepository petRepository,
                          com.petshop.backend.repository.ClienteRepository clienteRepository,
                          com.petshop.backend.repository.ServicoRepository servicoRepository) {
        this.filaService = filaService;
        this.petRepository = petRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    // ==========================================================
    // ROTAS WEB (JSP)
    // ==========================================================

    @GetMapping(value = {"/fila", "/FilaServlet"})
    public String exibirFila(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        // Listas vindas do banco (nada fixo no codigo)
        model.addAttribute("fila", filaService.listarTodos());
        model.addAttribute("servicos", servicoRepository.findAll());
        model.addAttribute("pets", petRepository.findAll());
        return "fila";
    }

    // ==========================================================
    // RF05 — TELAS DE BALCONISTA E VETERINARIO
    // O documento pede que a fila alterne estados "entre balconista
    // e veterinario/atendente".
    // ==========================================================

    @GetMapping("/balconista")
    public String telaBalconista(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("fila", filaService.listarTodos());
        model.addAttribute("pets", petRepository.findAll());
        model.addAttribute("servicos", servicoRepository.findAll());
        return "balconista";
    }

    @GetMapping("/veterinario")
    public String telaVeterinario(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("fila", filaService.listarTodos());
        return "veterinario";
    }

    /** Balconista registra a chegada escolhendo pet e servico das listas do banco. */
    @PostMapping("/balconista/registrar")
    public String registrarChegada(@RequestParam("idPet") Integer idPet,
                                   @RequestParam("idServico") Integer idServico,
                                   HttpSession session,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        try {
            filaService.adicionarPorId(idPet, idServico);
            redirect.addFlashAttribute("mensagem", "Pet adicionado à fila de espera.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Não foi possível adicionar: " + e.getMessage());
        }
        return "redirect:/balconista";
    }

    @GetMapping("/admin/agendamentos/novo")
    public String novoAgendamentoWeb(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        // Listas suspensas vindas do banco (nada fixo no codigo)
        model.addAttribute("pets", petRepository.findAll());
        model.addAttribute("servicos", servicoRepository.findAll());
        return "cadastro_agendamento";
    }

    @PostMapping("/admin/agendamentos/novo")
    public String salvarAgendamentoWeb(
            @RequestParam(value = "idPet", required = false) Integer idPet,
            @RequestParam(value = "idServico", required = false) Integer idServico,
            @RequestParam(value = "pet", required = false) String petNome,
            @RequestParam(value = "servico", required = false) String servicoNome,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        if (idPet != null && idServico != null) {
            filaService.adicionarPorId(idPet, idServico);
        } else {
            // compatibilidade com o formulario antigo
            filaService.adicionarPorNome(petNome, servicoNome);
        }
        redirect.addFlashAttribute("mensagem", "Pet adicionado à fila.");
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

        filaService.adicionarPorNome(pet, servico);
        ItemFilaDTO item = new ItemFilaDTO(0, cliente, pet, servico);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/api/fila/chamar-proximo")
    @ResponseBody
    public ResponseEntity<ItemFilaDTO> chamarProximoApi() {
        return ResponseEntity.ok(filaService.chamarProximo());
    }

    // ==========================================================
    // API DO APP — fila de um cliente especifico
    // (tela_meus_atendimentos.dart usa /api/fila/cliente/{cpf}
    //  e /api/fila/{id}/cancelar)
    // ==========================================================

    @GetMapping("/api/fila/cliente/{cpf}")
    @ResponseBody
    public ResponseEntity<List<java.util.Map<String, Object>>> filaDoClienteApi(
            @PathVariable("cpf") String cpf) {

        List<ItemFilaDTO> todos = filaService.listarTodos();

        // Pets do tutor: o app manda o CPF do tutor logado.
        // Alguns pets foram gravados com o n_documento do tutor em vez do CPF,
        // por isso buscamos pelos dois.
        java.util.List<com.petshop.backend.model.Pet> pets =
                new java.util.ArrayList<>(petRepository.findByCpfTutor(cpf));

        com.petshop.backend.model.Cliente tutor =
                clienteRepository.findById(cpf).orElse(null);
        if (tutor != null) {
            String doc = tutor.getNDocumento();
            if (doc != null && !doc.isBlank() && !doc.equals(cpf)) {
                pets.addAll(petRepository.findByCpfTutor(doc));
            }
        }

        // ArrayList exigido pelo RNF04
        java.util.List<java.util.Map<String, Object>> resposta = new java.util.ArrayList<>();
        int posicao = 0;

        for (ItemFilaDTO item : todos) {
            boolean doCliente = false;
            for (com.petshop.backend.model.Pet p : pets) {
                if (p.getNome() != null && p.getNome().equalsIgnoreCase(item.getNomePet())) {
                    doCliente = true;
                    break;
                }
            }
            if (!doCliente) {
                continue;
            }
            if ("Em espera".equalsIgnoreCase(item.getStatus())) {
                posicao++;
            }
            java.util.Map<String, Object> linha = new java.util.HashMap<>();
            linha.put("id", item.getId());
            linha.put("nomePet", item.getNomePet());
            linha.put("servico", item.getServico());
            linha.put("status", item.getStatus());
            linha.put("statusClass", item.getStatusClass());
            linha.put("posicao", "Em espera".equalsIgnoreCase(item.getStatus()) ? posicao : "-");
            linha.put("esperaMinutos", item.getMinutosEspera());
            linha.put("minutosEspera", item.getMinutosEspera());
            linha.put("horarioChegada", item.getHorarioChegada());
            linha.put("motivoCancelamento", item.getMotivoCancelamento());
            resposta.add(linha);
        }
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/api/fila/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> cancelarApi(
            @PathVariable("id") Integer id,
            @RequestParam(value = "cpf", required = false) String cpf,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        String motivo = body != null ? body.get("motivo") : null;
        if (motivo == null || motivo.isBlank()) {
            motivo = "Cancelado pelo cliente no aplicativo";
        }

        boolean ok = filaService.cancelar(id, motivo);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("sucesso", ok);
        resp.put("mensagem", ok
                ? "Atendimento cancelado."
                : "Não foi possível cancelar o atendimento.");
        return ResponseEntity.ok(resp);
    }
}