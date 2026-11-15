package com.petshop.backend.repository;

import com.petshop.backend.model.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Integer> {

    /** O caixa que esta aberto agora (horario_fechamento nulo). */
    Optional<Caixa> findFirstByHorarioFechamentoIsNullOrderByIdCaixaDesc();

    /** Historico: todos os caixas, do mais recente para o mais antigo. */
    List<Caixa> findAllByOrderByIdCaixaDesc();
}
