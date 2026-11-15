
package com.petshop.backend.repository;

import com.petshop.backend.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository
        extends JpaRepository<Venda, Integer> {
}
