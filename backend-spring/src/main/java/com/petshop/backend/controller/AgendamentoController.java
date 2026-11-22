package com.petshop.backend.controller;

import com.petshop.backend.repository.AgendaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.petshop.backend.model.Agenda;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AgendamentoController {

        private final AgendaRepository agendaRepository;

        public AgendamentoController(AgendaRepository agendaRepository) {
                this.agendaRepository = agendaRepository;
        }

        @GetMapping("/api/agendamentos/cliente/{cpf}")
        public ResponseEntity<List<Map<String, Object>>> listarAgendamentos(
                        @PathVariable String cpf) {

                return ResponseEntity.ok(
                                agendaRepository.buscarAgendamentosPorCpf(cpf));
        }

        @PostMapping("/api/agendamentos")
        public ResponseEntity<Map<String, Object>> criarAgendamento(
                        @RequestBody Map<String, Object> body) {

                Agenda agenda = new Agenda();

                agenda.setCpf((String) body.get("cpf"));

                agenda.setCodigoPet(
                                Integer.valueOf(body.get("codigo_pet").toString()));

                agenda.setCodigoServico(
                                Integer.valueOf(body.get("codigo_servico").toString()));

                agenda.setDataAgendamento(
                                LocalDate.parse(body.get("data_agendamento").toString()));

                agenda.setHoraAgendamento(
                                LocalTime.parse(body.get("hora_agendamento").toString()));

                agenda.setStatus("Agendado");

                agendaRepository.save(agenda);

                return ResponseEntity.ok(
                                Map.of(
                                                "sucesso", true,
                                                "mensagem", "Agendamento criado com sucesso"));
        }

        @PutMapping("/api/agendamentos/{id}/cancelar")
        public ResponseEntity<Map<String, Object>> cancelarAgendamento(
                        @PathVariable Integer id) {

                Agenda agenda = agendaRepository.findById(id).orElse(null);

                if (agenda == null) {
                        return ResponseEntity.status(404).body(
                                        Map.of(
                                                        "sucesso", false,
                                                        "mensagem", "Agendamento não encontrado"));
                }

                agenda.setStatus("Cancelado");

                agendaRepository.save(agenda);

                return ResponseEntity.ok(
                                Map.of(
                                                "sucesso", true,
                                                "mensagem", "Agendamento cancelado com sucesso"));
        }
}