package com.petshop.backend.controller;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.model.Pet;
import com.petshop.backend.model.Tutela;
import com.petshop.backend.model.Caixa;
import com.petshop.backend.repository.CaixaRepository;
import com.petshop.backend.repository.ClienteRepository;
import com.petshop.backend.repository.PetRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** Somente operacoes de PET: tela, salvar, excluir e as rotas do app Flutter. */
@Controller
@CrossOrigin(origins = "*")
public class PetController {

    private final PetRepository petRepository;
    private final ClienteRepository clienteRepository;
    private final CaixaRepository caixaRepository;
    private final com.petshop.backend.repository.TutelaRepository tutelaRepository;

    public PetController(PetRepository petRepository,
                         ClienteRepository clienteRepository,
                         CaixaRepository caixaRepository,
                         com.petshop.backend.repository.TutelaRepository tutelaRepository) {
        this.petRepository = petRepository;
        this.clienteRepository = clienteRepository;
        this.caixaRepository = caixaRepository;
        this.tutelaRepository = tutelaRepository;
    }

    @GetMapping("/pets")
    public String listarPetsWeb(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("pets", petRepository.findAll());
        model.addAttribute("clientes", clienteRepository.findAll());
        return "pets";
    }

    @PostMapping("/admin/pets/salvar")
    public String salvarPetWeb(@RequestParam(value = "id", required = false) Integer id,
                               @RequestParam("nome") String nome,
                               @RequestParam("raca") String raca,
                               @RequestParam("especie") String especie,
                               @RequestParam(value = "idade", required = false, defaultValue = "1") Integer idade,
                               @RequestParam("cpfCliente") String cpfCliente,
                               HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        Cliente cliente = clienteRepository.findById(cpfCliente).orElse(null);
        if (cliente != null) {
            Pet pet = (id != null) ? petRepository.findById(id).orElse(new Pet()) : new Pet();
            pet.setNome(nome);
            pet.setRaca(raca);
            pet.setEspecie(especie);
            pet.setIdade(idade);
            Pet salvo = petRepository.save(pet);
            tutelaRepository.deleteByCodigoPet(salvo.getId());
            Tutela vinculo = new Tutela();
            vinculo.setCodigoPet(salvo.getId());
            vinculo.setCpf(cliente.getCpf());
            tutelaRepository.save(vinculo);
        }
        return "redirect:/pets";
    }

    @PostMapping("/admin/pets/excluir")
    public String excluirPetWeb(@RequestParam("id") Integer id, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        tutelaRepository.deleteByCodigoPet(id);
        petRepository.deleteById(id);
        return "redirect:/pets";
    }

    // ==========================================================
    // APIS REST (FLUTTER)
    // ==========================================================

    @GetMapping("/tutor/{cpf}/pets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPetsDoTutor(@PathVariable("cpf") String cpf) {
        // A propriedade do pet vem somente do vínculo oficial, nunca do documento do pet.
        java.util.List<Pet> pets = new java.util.ArrayList<>();
        for (Integer idPet : tutelaRepository.findCodigoPetByCpf(cpf)) {
            petRepository.findById(idPet).ifPresent(pets::add);
        }
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Pet p : pets) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("nome", p.getNome());
            item.put("especie", p.getEspecie());
            item.put("raca", p.getRaca());
            item.put("porte", p.getPorte());
            item.put("peso", p.getPeso());
            item.put("n_documento", p.getNDocumento());
            item.put("nDocumento", p.getNDocumento());
            item.put("idade", p.getIdade() != null ? p.getIdade() : 1);
            lista.add(item);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("pets", lista);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cadastrar/pet")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cadastrarPetMobile(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        String cpf = (String) body.get("cpf");
        String nome = (String) body.get("nome");
        String especie = (String) body.get("especie");
        String raca = (String) body.getOrDefault("raca", "Sem raça definida");
        Integer idade = body.get("idade") != null ? Integer.valueOf(body.get("idade").toString()) : 1;

        Optional<Cliente> optCliente = clienteRepository.findById(cpf);
        if (optCliente.isEmpty()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "Cliente não encontrado pelo CPF informado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Pet pet = new Pet(nome, raca, especie, idade, optCliente.get());
        pet.setNDocumento(body.get("n_documento") == null ? null : body.get("n_documento").toString());
        Pet salvo = petRepository.save(pet);

        // Garante inserção na tabela de vínculo tutela
        try {
            Tutela t = new Tutela();
            t.setCodigoPet(salvo.getId());
            t.setCpf(optCliente.get().getCpf());
            tutelaRepository.save(t);
        } catch (Exception e) {
            // tutela já existente ou ignorada
        }

        resp.put("sucesso", true);
        resp.put("mensagem", "Pet cadastrado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // ==========================================================
    // API DO APP — pet individual
    // (o Flutter usa estas rotas em tela_pets.dart e tela_edicao_pet.dart)
    // ==========================================================

    @GetMapping("/api/pets/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarPetApi(@PathVariable("id") Integer id) {
        Pet p = petRepository.findById(id).orElse(null);
        if (p == null) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("mensagem", "Pet não encontrado");
            return ResponseEntity.status(404).body(erro);
        }
        return ResponseEntity.ok(petParaMapa(p));
    }

    @PutMapping("/api/pets/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarPetApi(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> body) {

        Pet p = petRepository.findById(id).orElse(null);
        if (p == null) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("mensagem", "Pet não encontrado");
            return ResponseEntity.status(404).body(erro);
        }

        if (body.get("peso") != null && !body.get("peso").toString().isBlank()) {
            try {
                p.setPeso(new java.math.BigDecimal(body.get("peso").toString()));
            } catch (NumberFormatException ignored) { }
        }
        if (body.get("idade") != null && !body.get("idade").toString().isBlank()) {
            try {
                p.setIdade(Integer.valueOf(body.get("idade").toString()));
            } catch (NumberFormatException ignored) { }
        }
        if (body.get("n_documento") != null && !body.get("n_documento").toString().isBlank()) {
            p.setNDocumento(body.get("n_documento").toString());
        }

        petRepository.save(p);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("mensagem", "Pet atualizado com sucesso!");
        resp.put("pet", petParaMapa(p));
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/api/pets/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> excluirPetApi(
            @PathVariable("id") Integer id,
            @RequestParam(value = "cpf", required = false) String cpf) {

        if (!petRepository.existsById(id)) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("mensagem", "Pet não encontrado");
            return ResponseEntity.status(404).body(erro);
        }
        tutelaRepository.deleteByCodigoPet(id);
        petRepository.deleteById(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("mensagem", "Pet excluído com sucesso.");
        return ResponseEntity.ok(resp);
    }

    /** Monta o JSON de um pet do mesmo jeito que a listagem do tutor. */
    private Map<String, Object> petParaMapa(Pet p) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", p.getId());
        item.put("codigoPet", p.getId());
        item.put("nome", p.getNome());
        item.put("especie", p.getEspecie());
        item.put("raca", p.getRaca());
        item.put("porte", p.getPorte());
        item.put("peso", p.getPeso());
        item.put("idade", p.getIdade() != null ? p.getIdade() : 1);
        item.put("n_documento", p.getNDocumento());
        item.put("nDocumento", p.getNDocumento());
        return item;
    }
}
