package com.petshop.backend.controller;

import com.petshop.backend.model.Cliente;
import com.petshop.backend.model.Pet;
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

    public PetController(PetRepository petRepository,
                         ClienteRepository clienteRepository,
                         CaixaRepository caixaRepository) {
        this.petRepository = petRepository;
        this.clienteRepository = clienteRepository;
        this.caixaRepository = caixaRepository;
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
            pet.setCpfTutor(cliente.getCpf());
            petRepository.save(pet);
        }
        return "redirect:/pets";
    }

    @PostMapping("/admin/pets/excluir")
    public String excluirPetWeb(@RequestParam("id") Integer id, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        petRepository.deleteById(id);
        return "redirect:/pets";
    }

    // ==========================================================
    // APIS REST (FLUTTER)
    // ==========================================================

    @GetMapping("/tutor/{cpf}/pets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPetsDoTutor(@PathVariable("cpf") String cpf) {
        // Alguns pets do banco foram gravados com o num_documento do tutor
        // (que nao e o CPF). Buscamos pelos dois campos.
        java.util.List<Pet> pets = new java.util.ArrayList<>(petRepository.findByCpfTutor(cpf));
        Cliente tutor = clienteRepository.findById(cpf).orElse(null);
        if (tutor != null) {
            String doc = tutor.getNDocumento();
            if (doc != null && !doc.isBlank() && !doc.equals(cpf)) {
                pets.addAll(petRepository.findByCpfTutor(doc));
            }
        }
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Pet p : pets) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("nome", p.getNome());
            item.put("especie", p.getEspecie());
            item.put("raca", p.getRaca());
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
        petRepository.save(pet);

        resp.put("sucesso", true);
        resp.put("mensagem", "Pet cadastrado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
