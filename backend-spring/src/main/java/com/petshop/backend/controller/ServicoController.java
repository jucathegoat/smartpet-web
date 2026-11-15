package com.petshop.backend.controller;

import com.petshop.backend.model.Servico;
import com.petshop.backend.repository.ServicoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicos do pet shop.
 *
 * O app Flutter usava uma lista fixa de servicos no codigo.
 * Este endpoint entrega a lista real que esta na tabela "servico".
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ServicoController {

    private final ServicoRepository servicoRepository;

    public ServicoController(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @GetMapping("/servicos")
    public ResponseEntity<Map<String, Object>> listarServicos() {
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Servico s : servicoRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("codigo", s.getIdServico());
            item.put("nome", s.getNome());
            item.put("preco", s.getPreco());
            item.put("categoria", s.getCategoria());
            lista.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("servicos", lista);
        return ResponseEntity.ok(resp);
    }
}
