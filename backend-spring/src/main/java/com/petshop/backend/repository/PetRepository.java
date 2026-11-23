package com.petshop.backend.repository;

import com.petshop.backend.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
    List<Pet> findByNDocumento(String nDocumento);

    /** Usado pelo formulario da fila, que informa o nome do pet. */
    List<Pet> findByNomeIgnoreCase(String nome);
}
