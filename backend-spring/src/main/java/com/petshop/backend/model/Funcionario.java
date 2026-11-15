
package com.petshop.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "funcionario")
public class Funcionario {

    @Id
    @Column(name = "matricula")
    private Integer matricula;

    @Column(name = "crmv", length = 20)
    private String crmv;

    @Column(name = "cargo", length = 40, nullable = false)
    private String cargo;

    @Column(name = "cpf", nullable = false)
    private Integer cpf;

    @Column(name = "n_documento", nullable = false)
    private Integer nDocumento;

    @Column(name = "nome", nullable = false)
    private String nome;

    public Funcionario() {
        this.matricula = gerarMatricula();
    }

    

    /*método que gera uma matrícula aleatória. ainda não é executado automaticamente quando um funcionário é cadastrado.
    Para gerar a matrícula automaticamente ao criar a entidade vamos chamar esse método no construtor posteriormente*/
    private Integer gerarMatricula() {
        return java.util.concurrent.ThreadLocalRandom
                .current()
                .nextInt(10000, 100000);
    }
}
