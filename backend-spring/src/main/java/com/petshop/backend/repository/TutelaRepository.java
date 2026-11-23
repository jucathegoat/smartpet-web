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

    void deleteByCodigoPet(Integer codigoPet);

    @Query("SELECT t.codigoPet FROM Tutela t WHERE t.cpf = :cpf")
    List<Integer> findCodigoPetByCpf(@Param("cpf") String cpf);

    @Query("SELECT t.cpf FROM Tutela t WHERE t.codigoPet = :codigoPet")
    List<String> findCpfByCodigoPet(@Param("codigoPet") Integer codigoPet);
}
