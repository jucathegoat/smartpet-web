package com.petshop.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_pet")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "raca", length = 50)
    private String raca;

    @Column(name = "especie", nullable = false, length = 50)
    private String especie;

    @Column(name = "idade")
    private Integer idade;

    @Column(name = "porte", length = 25)
    private String porte;

    @Column(name = "peso", precision = 5, scale = 2)
    private BigDecimal peso;

    // Guardado como texto: alguns pets do banco apontam para tutores que nao existem,
    // e um relacionamento @ManyToOne derrubaria a listagem inteira.
    @Column(name = "n_documento", length = 20)
    private String cpfTutor;

    public Pet() {}

    public Pet(String nome, String raca, String especie, Integer idade, Cliente cliente) {
        this.nome = nome;
        this.raca = raca;
        this.especie = especie;
        this.idade = idade;
        this.cpfTutor = (cliente != null) ? cliente.getCpf() : null;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public String getCpfTutor() { return cpfTutor; }
    public void setCpfTutor(String cpfTutor) { this.cpfTutor = cpfTutor; }
}