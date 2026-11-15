package com.petshop.backend.repository;

import com.petshop.backend.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DespesaRepository extends JpaRepository<Despesa, Integer> {
    List<Despesa> findByIdCaixaOrderByDataHoraDesc(Integer idCaixa);
}
