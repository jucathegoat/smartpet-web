package com.petshop.backend.repository;

import com.petshop.backend.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Integer> {
    List<Receita> findByIdCaixaOrderByDataHoraDesc(Integer idCaixa);
}
