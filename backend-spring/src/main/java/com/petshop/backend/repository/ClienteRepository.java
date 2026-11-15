package com.petshop.backend.repository;

import com.petshop.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByUsuario(String usuario);
}