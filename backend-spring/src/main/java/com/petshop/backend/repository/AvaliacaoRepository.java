package com.petshop.backend.repository;

import com.petshop.backend.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {
    List<Avaliacao> findByRespondidaFalse();
    Optional<Avaliacao> findByServicoId(Integer servicoId);
}