package com.petshop.backend.controller;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.petshop.backend.model.Caixa;
import com.petshop.backend.model.Despesa;
import com.petshop.backend.model.Receita;
import com.petshop.backend.repository.CaixaRepository;
import com.petshop.backend.repository.DespesaRepository;
import com.petshop.backend.repository.ReceitaRepository;

@RestController
@RequestMapping("/caixa-api")
@CrossOrigin(origins = "*")
public class CaixaApiController {

    private final CaixaRepository caixaRepository;
    private final ReceitaRepository receitaRepository;
    private final DespesaRepository despesaRepository;

    public CaixaApiController(CaixaRepository caixaRepository,
                              ReceitaRepository receitaRepository,
                              DespesaRepository despesaRepository) {
        this.caixaRepository = caixaRepository;
        this.receitaRepository = receitaRepository;
        this.despesaRepository = despesaRepository;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> resp = new HashMap<>();
        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();

        if (opt.isEmpty()) {
            resp.put("aberto", false);
            resp.put("saldo", BigDecimal.ZERO);
            return resp;
        }

        Caixa caixa = opt.get();
        BigDecimal entradas = BigDecimal.ZERO;
        for (Receita r : receitaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            entradas = entradas.add(r.getValor() != null ? r.getValor() : BigDecimal.ZERO);
        }
        BigDecimal saidas = BigDecimal.ZERO;
        for (Despesa d : despesaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            saidas = saidas.add(d.getValor() != null ? d.getValor() : BigDecimal.ZERO);
        }

        BigDecimal inicial = caixa.getSaldoInicial() != null ? caixa.getSaldoInicial() : BigDecimal.ZERO;

        resp.put("aberto", true);
        resp.put("idCaixa", caixa.getIdCaixa());
        resp.put("saldoInicial", inicial);
        resp.put("entradas", entradas);
        resp.put("saidas", saidas);
        resp.put("saldo", inicial.add(entradas).subtract(saidas));
        return resp;
    }

    @PostMapping("/abrir")
    public Map<String, Object> abrir(@RequestParam(value = "saldoInicial", required = false) String saldoInicial) {
        Map<String, Object> resp = new HashMap<>();

        if (caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc().isPresent()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "Já existe um caixa aberto.");
            return resp;
        }

        Caixa c = new Caixa(parseValor(saldoInicial), LocalDateTime.now());
        caixaRepository.save(c);

        resp.put("sucesso", true);
        resp.put("idCaixa", c.getIdCaixa());
        return resp;
    }

    @PostMapping("/fechar")
    public Map<String, Object> fechar() {
        Map<String, Object> resp = new HashMap<>();
        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();

        if (opt.isEmpty()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "Nenhum caixa aberto.");
            return resp;
        }

        Caixa caixa = opt.get();
        BigDecimal entradas = BigDecimal.ZERO;
        for (Receita r : receitaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            entradas = entradas.add(r.getValor() != null ? r.getValor() : BigDecimal.ZERO);
        }
        BigDecimal saidas = BigDecimal.ZERO;
        for (Despesa d : despesaRepository.findByIdCaixaOrderByDataHoraDesc(caixa.getIdCaixa())) {
            saidas = saidas.add(d.getValor() != null ? d.getValor() : BigDecimal.ZERO);
        }
        BigDecimal inicial = caixa.getSaldoInicial() != null ? caixa.getSaldoInicial() : BigDecimal.ZERO;

        caixa.setSaldoFinal(inicial.add(entradas).subtract(saidas));
        caixa.setHorarioFechamento(LocalDateTime.now());
        caixaRepository.save(caixa);

        resp.put("sucesso", true);
        resp.put("saldoFinal", caixa.getSaldoFinal());
        return resp;
    }

    @PostMapping("/movimento")
    public Map<String, Object> movimento(@RequestParam("tipo") String tipo,
                                         @RequestParam(value = "categoria", required = false) String categoria,
                                         @RequestParam(value = "descricao", required = false) String descricao,
                                         @RequestParam("valor") String valor) {
        Map<String, Object> resp = new HashMap<>();
        Optional<Caixa> opt = caixaRepository.findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();

        if (opt.isEmpty()) {
            resp.put("sucesso", false);
            resp.put("mensagem", "Abra o caixa antes de lançar movimentos.");
            return resp;
        }

        Integer idCaixa = opt.get().getIdCaixa();
        BigDecimal v = parseValor(valor);

        if ("saida".equalsIgnoreCase(tipo)) {
            despesaRepository.save(new Despesa(categoria, descricao, v, LocalDateTime.now(), idCaixa));
        } else {
            receitaRepository.save(new Receita(categoria, descricao, v, LocalDateTime.now(), idCaixa));
        }

        resp.put("sucesso", true);
        return resp;
    }

    private static BigDecimal parseValor(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String limpo = s.trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
        if (limpo.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(limpo); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
}
