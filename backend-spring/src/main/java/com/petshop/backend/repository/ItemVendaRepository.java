package com.petshop.backend.repository;

import com.petshop.backend.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Integer> {

    List<ItemVenda> findByIdVenda(Integer idVenda);
}
