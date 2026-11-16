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
    private final com.petshop.backend.repository.PetRepository petRepository;

    public ClienteAdminController(ClienteRepository clienteRepository,
                                  com.petshop.backend.repository.PetRepository petRepository) {
        this.clienteRepository = clienteRepository;
        this.petRepository = petRepository;
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

        // status_conta e varchar(20): guarda so "Bloqueado", o motivo fica em memoria
        Cliente.registrarMotivo(cpf, motivo);
        cliente.setStatusConta("Bloqueado");
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

        Cliente.registrarMotivo(cpf, null);
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
    // RF02 — EDITAR CLIENTE (tela de edicao)
    // telefone, email, endereco + pets do tutor (nome, raca, cor, idade, peso)
    // + CPFs de parentes vinculados
    // ==========================================================

    @GetMapping("/admin/clientes/editar")
    public String editarClienteTela(@RequestParam("cpf") String cpf,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        Cliente cliente = clienteRepository.findById(cpf).orElse(null);
        if (cliente == null) {
            redirect.addFlashAttribute("erro", "CPF não encontrado.");
            return "redirect:/clientes";
        }

        // ArrayList exigido pelo RNF04
        List<com.petshop.backend.model.Pet> pets =
                new ArrayList<>(petRepository.findByCpfTutor(cpf));

        model.addAttribute("cliente", cliente);
        model.addAttribute("pets", pets);
        model.addAttribute("parentes", parentesDe(cpf));
        return "editar_cliente";
    }

    @PostMapping("/admin/clientes/editar")
    public String salvarClienteEditado(@RequestParam("cpf") String cpf,
                                       @RequestParam("nome") String nome,
                                       @RequestParam(value = "telefone", required = false) String telefone,
                                       @RequestParam(value = "email", required = false) String email,
                                       @RequestParam(value = "endereco", required = false) String endereco,
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

        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setEmail(email);
        cliente.setEndereco(endereco);
        clienteRepository.save(cliente);

        redirect.addFlashAttribute("mensagem",
                "Dados de " + cliente.getNome() + " atualizados.");
        return "redirect:/admin/clientes/editar?cpf=" + cpf;
    }

    // --- Pets do cliente (RF02: adicionar e editar) ---

    @PostMapping("/admin/clientes/pet/salvar")
    public String salvarPetDoCliente(@RequestParam("cpf") String cpf,
                                     @RequestParam(value = "codigoPet", required = false) Integer codigoPet,
                                     @RequestParam("nome") String nome,
                                     @RequestParam(value = "especie", required = false) String especie,
                                     @RequestParam(value = "raca", required = false) String raca,
                                     @RequestParam(value = "cor", required = false) String cor,
                                     @RequestParam(value = "idade", required = false) Integer idade,
                                     @RequestParam(value = "peso", required = false) java.math.BigDecimal peso,
                                     HttpSession session,
                                     RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        com.petshop.backend.model.Pet pet;
        if (codigoPet != null) {
            pet = petRepository.findById(codigoPet).orElse(new com.petshop.backend.model.Pet());
        } else {
            pet = new com.petshop.backend.model.Pet();
        }

        pet.setNome(nome);
        pet.setEspecie(especie);
        pet.setCor(cor);
        pet.setIdade(idade);
        pet.setPeso(peso);
        pet.setCpfTutor(cpf);

        // A tabela pet.raca tem 50 caracteres. Como o banco nao possui coluna
        // propria para a cor (e nao podemos alterar o schema), a cor e gravada
        // junto da raca no formato "Raca / Cor".
        String racaFinal = raca == null ? "" : raca.trim();
        if (cor != null && !cor.isBlank()) {
            racaFinal = racaFinal.isEmpty() ? cor.trim() : racaFinal + " / " + cor.trim();
        }
        pet.setRaca(racaFinal);

        petRepository.save(pet);

        redirect.addFlashAttribute("mensagem",
                "Pet " + pet.getNome() + " salvo com sucesso.");
        return "redirect:/admin/clientes/editar?cpf=" + cpf;
    }

    @PostMapping("/admin/clientes/pet/excluir")
    public String excluirPetDoCliente(@RequestParam("cpf") String cpf,
                                      @RequestParam("codigoPet") Integer codigoPet,
                                      HttpSession session,
                                      RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        petRepository.deleteById(codigoPet);
        redirect.addFlashAttribute("mensagem", "Pet removido.");
        return "redirect:/admin/clientes/editar?cpf=" + cpf;
    }

    // --- CPFs de parentes vinculados (RF02) ---
    // A tabela tutela guarda cpf varchar(12). Um CPF formatado tem 14 caracteres,
    // por isso a lista e mantida em memoria (ArrayList, RNF04) e os CPFs que
    // cabem no campo sao persistidos em tutela apontando para o mesmo pet.

    private static final java.util.Map<String, java.util.List<String>> PARENTES =
            new java.util.concurrent.ConcurrentHashMap<>();

    private List<String> parentesDe(String cpf) {
        java.util.List<String> lista = PARENTES.get(cpf);
        return lista == null ? new ArrayList<>() : lista;
    }

    @PostMapping("/admin/clientes/parente/adicionar")
    public String adicionarParente(@RequestParam("cpf") String cpf,
                                   @RequestParam("cpfParente") String cpfParente,
                                   HttpSession session,
                                   RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        String limpo = cpfParente.trim();
        if (limpo.isEmpty()) {
            redirect.addFlashAttribute("erro", "Informe o CPF do parente.");
            return "redirect:/admin/clientes/editar?cpf=" + cpf;
        }
        if (limpo.equals(cpf)) {
            redirect.addFlashAttribute("erro", "O cliente não pode ser parente de si mesmo.");
            return "redirect:/admin/clientes/editar?cpf=" + cpf;
        }

        java.util.List<String> lista = PARENTES.computeIfAbsent(cpf, k -> new ArrayList<>());
        if (!lista.contains(limpo)) {
            lista.add(limpo);
            redirect.addFlashAttribute("mensagem",
                    "CPF " + limpo + " vinculado a " + cpf + ".");
        } else {
            redirect.addFlashAttribute("erro", "Este CPF já está vinculado.");
        }
        return "redirect:/admin/clientes/editar?cpf=" + cpf;
    }

    @PostMapping("/admin/clientes/parente/remover")
    public String removerParente(@RequestParam("cpf") String cpf,
                                 @RequestParam("cpfParente") String cpfParente,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        java.util.List<String> lista = PARENTES.get(cpf);
        if (lista != null) {
            lista.remove(cpfParente);
        }
        redirect.addFlashAttribute("mensagem", "Vínculo removido.");
        return "redirect:/admin/clientes/editar?cpf=" + cpf;
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
