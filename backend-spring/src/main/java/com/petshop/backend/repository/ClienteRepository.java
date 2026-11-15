package com.petshop.backend.repository;

import com.petshop.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {

    Optional<Cliente> findByUsuario(String usuario);

    /** RF01 — busca por CPF parcial (usuario digita so alguns digitos). */
    List<Cliente> findByCpfContaining(String cpf);

    /** RF01 — busca por nome, ignorando maiusculas. */
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
