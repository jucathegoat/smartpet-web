package com.petshop.backend.repository;

import com.petshop.backend.model.Tutela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Vinculo oficial entre pet e tutor (tabela `tutela`). */
@Repository
public interface TutelaRepository extends JpaRepository<Tutela, Tutela.TutelaId> {

    @Query("SELECT t.id.codigoPet FROM Tutela t WHERE t.id.cpf = :cpf")
    List<Integer> findCodigoPetByCpf(@Param("cpf") String cpf);
}
