package com.petshop.backend.repository;

import com.petshop.backend.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Integer> {

    @Query(value =
            "SELECT a.id_agenda AS id, " +
            "p.codigo_pet AS pet_id, " +
            "p.nome AS nomePet, " +
            "p.especie AS especie, " +
            "p.idade AS idade, " +
            "s.codigo_servico AS servico_id, " +
            "s.nome AS servico, " +
            "s.categoria AS categoria, " +
            "a.data_agendamento AS data_agendamento, " +
            "a.hora_agendamento AS hora_agendamento, " +
            "a.status AS status " +
            "FROM agenda a " +
            "INNER JOIN pet p ON p.codigo_pet = a.codigo_pet " +
            "INNER JOIN servico s ON s.codigo_servico = a.codigo_servico " +
            "WHERE a.cpf = ?1 " +
            "ORDER BY a.data_agendamento, a.hora_agendamento",
            nativeQuery = true)
    List<Map<String, Object>> buscarAgendamentosPorCpf(String cpf);
}