package com.petshop.backend.controller;

import com.petshop.backend.model.Agenda;
import com.petshop.backend.model.Tutela;
import com.petshop.backend.repository.AgendaRepository;
import com.petshop.backend.repository.TutelaRepository;
import com.petshop.backend.service.FilaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private final AgendaRepository agendaRepository;
    private final TutelaRepository tutelaRepository;
    private final FilaService filaService;

    public AgendamentoController(AgendaRepository agendaRepository,
                                 TutelaRepository tutelaRepository,
                                 FilaService filaService) {
        this.agendaRepository = agendaRepository;
        this.tutelaRepository = tutelaRepository;
        this.filaService = filaService;
    }

    @GetMapping("/agendamentos")
    public String painelAgendamentos(HttpSession session, Model model) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("menuAtivo", "/agendamentos");
        model.addAttribute("agendamentos", agendaRepository.listarTodosAgendamentos());
        return "agendamentos";
    }

    @GetMapping("/api/agendamentos/cliente/{cpf}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarAgendamentos(@PathVariable String cpf) {
        return ResponseEntity.ok(agendaRepository.buscarAgendamentosPorCpf(cpf));
    }

    @PostMapping("/api/agendamentos")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> criarAgendamento(
            @RequestBody Map<String, Object> body) {
        String cpf = body.get("cpf") != null ? body.get("cpf").toString().trim() : "";
        if (cpf.isEmpty()
                || body.get("codigo_pet") == null
                || body.get("codigo_servico") == null
                || body.get("data_agendamento") == null
                || body.get("hora_agendamento") == null) {
            return respostaErro(HttpStatus.BAD_REQUEST, "Informe tutor, pet, serviço, data e horário.");
        }

        final Integer codigoPet;
        final Integer codigoServico;
        final LocalDate data;
        final LocalTime hora;
        try {
            codigoPet = Integer.valueOf(body.get("codigo_pet").toString());
            codigoServico = Integer.valueOf(body.get("codigo_servico").toString());
            data = LocalDate.parse(body.get("data_agendamento").toString());
            hora = LocalTime.parse(body.get("hora_agendamento").toString());
        } catch (RuntimeException e) {
            return respostaErro(HttpStatus.BAD_REQUEST, "Data, horário ou identificador inválido.");
        }

        boolean petDoTutor = tutelaRepository.findAll().stream().anyMatch(vinculo ->
                Objects.equals(vinculo.getCodigoPet(), codigoPet)
                        && normalizarCpf(vinculo.getCpf()).equals(normalizarCpf(cpf)));
        if (!petDoTutor) {
            return respostaErro(HttpStatus.NOT_FOUND, "Pet não vinculado a este tutor.");
        }

        Agenda agenda = new Agenda();
        agenda.setCpf(cpf);
        agenda.setCodigoPet(codigoPet);
        agenda.setCodigoServico(codigoServico);
        agenda.setDataAgendamento(data);
        agenda.setHoraAgendamento(hora);
        agenda.setStatus("Agendado");

        Agenda salvo = agendaRepository.save(agenda);

        // Mantém o fluxo atual da recepção: o agendamento também entra na fila.
        filaService.adicionarPorId(codigoPet, codigoServico);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("sucesso", true);
        resposta.put("id", salvo.getId());
        resposta.put("mensagem", "Agendamento criado com sucesso.");
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/api/agendamentos/{id}/cancelar")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> cancelarAgendamento(@PathVariable Integer id) {
        Agenda agenda = agendaRepository.findById(id).orElse(null);
        if (agenda == null) {
            return respostaErro(HttpStatus.NOT_FOUND, "Agendamento não encontrado.");
        }

        agenda.setStatus("Cancelado");
        agendaRepository.save(agenda);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("sucesso", true);
        resposta.put("mensagem", "Agendamento cancelado com sucesso.");
        return ResponseEntity.ok(resposta);
    }

    private ResponseEntity<Map<String, Object>> respostaErro(HttpStatus status, String mensagem) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("sucesso", false);
        resposta.put("mensagem", mensagem);
        return ResponseEntity.status(status).body(resposta);
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
