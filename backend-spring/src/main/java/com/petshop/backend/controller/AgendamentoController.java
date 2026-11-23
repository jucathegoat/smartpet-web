package com.petshop.backend.controller;

import com.petshop.backend.model.Agenda;
import com.petshop.backend.repository.AgendaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class AgendamentoController {

        private final AgendaRepository agendaRepository;

        public AgendamentoController(AgendaRepository agendaRepository) {
                this.agendaRepository = agendaRepository;
        }

        @GetMapping("/agendamentos")
        public String painelAgendamentos(HttpSession session, Model model) {
                if (session.getAttribute("adminLogado") == null) {
                        return "redirect:/login";
                }
                model.addAttribute("menuAtivo", "/agendamentos");
                model.addAttribute("agendamentos", agendaRepository.listarTodosAgendamentosPainel());
                return "agendamentos";
        }

        @GetMapping("/api/agendamentos/cliente/{cpf}")
        @ResponseBody
        public ResponseEntity<List<Map<String, Object>>> listarAgendamentos(
                        @PathVariable String cpf) {

                return ResponseEntity.ok(
                                agendaRepository.buscarAgendamentosPorCpf(cpf));
        }

        @PostMapping("/api/agendamentos")
        @ResponseBody
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
        @ResponseBody
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