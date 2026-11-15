
package com.petshop.backend.repository;

import com.petshop.backend.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository
        extends JpaRepository<Funcionario, Integer> {
}
