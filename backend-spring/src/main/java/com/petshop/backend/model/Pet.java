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

    /** Cor do pet. Nao existe coluna propria no banco: fica gravada junto da raca. */
    @Transient
    private String cor;

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

    // Documento proprio do pet. O tutor e definido exclusivamente pela tabela tutela.
    @Column(name = "n_documento", length = 20)
    private String nDocumento;

    public Pet() {}

    public Pet(String nome, String raca, String especie, Integer idade, Cliente cliente) {
        this.nome = nome;
        this.raca = raca;
        this.especie = especie;
        this.idade = idade;
        this.nDocumento = null;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public String getNDocumento() { return nDocumento; }
    public void setNDocumento(String nDocumento) { this.nDocumento = nDocumento; }
}
