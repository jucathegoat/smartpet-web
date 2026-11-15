
package com.petshop.backend.repository;

import com.petshop.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository
        extends JpaRepository<Produto, Integer> {
}
