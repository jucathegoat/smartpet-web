package com.petshop.backend.service;

import com.petshop.backend.model.*;
import com.petshop.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    // Desativado: o banco do Aiven ja tem os dados reais (tabela tutor).
    // Ligar apenas em banco vazio, com -Dsmartpet.seed=true
    @org.springframework.beans.factory.annotation.Value("${smartpet.seed:false}")
    private boolean seed;

    private final ClienteRepository clienteRepository;
    private final PetRepository petRepository;
    private final ProdutoRepository produtoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public DataInitializer(ClienteRepository clienteRepository,
                           PetRepository petRepository,
                           ProdutoRepository produtoRepository,
                           AvaliacaoRepository avaliacaoRepository,
                           NotificacaoRepository notificacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.petRepository = petRepository;
        this.produtoRepository = produtoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seed) { return; }
        if (clienteRepository.count() == 0) {
            Cliente c1 = new Cliente("12345678900", "Carlos Silva", "carlos@email.com", "(11) 98888-1111", "Rua das Flores, 123", "carlos", "123456");
            Cliente c2 = new Cliente("98765432100", "Maria Oliveira", "maria@email.com", "(11) 97777-2222", "Av. Brasil, 456", "maria", "123456");
            clienteRepository.save(c1);
            clienteRepository.save(c2);

            Pet p1 = new Pet("Thor", "Golden Retriever", "Cachorro", 4, c1);
            Pet p2 = new Pet("Garfield", "Persa", "Gato", 2, c2);
            petRepository.save(p1);
            petRepository.save(p2);
        }

        if (produtoRepository.count() == 0) {
            produtoRepository.save(new Produto("Ração Golden Cães Adultos 15kg", "Golden", "Alimento", 25, new BigDecimal("189.90")));
            produtoRepository.save(new Produto("Shampoo Pet Neutro 500ml", "PetClean", "Higiene", 40, new BigDecimal("32.50")));
            produtoRepository.save(new Produto("Coleira Antipulgas Seresto", "Bayer", "Acessórios", 15, new BigDecimal("249.00")));
            produtoRepository.save(new Produto("Brinquedo Mordedor Osso", "Petz", "Brinquedos", 50, new BigDecimal("19.90")));
        }

        if (avaliacaoRepository.count() == 0) {
            avaliacaoRepository.save(new Avaliacao(1, "Thor", "Banho e Tosa Completa", null, null, null, false));
            avaliacaoRepository.save(new Avaliacao(2, "Garfield", "Consulta Geral", 3, LocalDateTime.now().minusDays(1), "Ótimo atendimento!", true));
        }

        if (notificacaoRepository.count() == 0) {
            notificacaoRepository.save(new Notificacao("Bem-vindo ao SmartPet!", "Seu aplicativo está conectado e pronto para agendamentos e compras.", LocalDateTime.now()));
            notificacaoRepository.save(new Notificacao("Lembrete de Vacina", "A vacina de reforço anual do seu pet está próxima.", LocalDateTime.now().minusHours(2)));
        }
    }
}