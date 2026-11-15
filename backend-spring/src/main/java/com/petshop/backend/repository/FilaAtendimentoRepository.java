package com.petshop.backend.repository;

import com.petshop.backend.model.FilaAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilaAtendimentoRepository
        extends JpaRepository<FilaAtendimento, Integer> {

    /** Itens na ordem em que entraram na fila (mais antigo primeiro). */
    List<FilaAtendimento> findAllByOrderByDataHoraEntradaAsc();

    /** Primeiro da fila que ainda nao foi chamado (RF05). */
    Optional<FilaAtendimento> findFirstByStatusOrderByDataHoraEntradaAsc(String status);

    /** Historico filtrado por situacao. */
    List<FilaAtendimento> findByStatusOrderByDataHoraEntradaAsc(String status);
}
