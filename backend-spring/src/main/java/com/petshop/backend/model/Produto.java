package com.petshop.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer codigo;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "marca", length = 255)
    private String marca;

    @Column(name = "tipo", length = 100)
    private String categoria;

    @Column(name = "estoque", nullable = false)
    private Integer estoque;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /** Link da foto do produto (coluna url, ja existente no banco). */
    @Column(name = "url", length = 500)
    private String url;

    public Produto() {}

    public Produto(String nome, String marca, String categoria, Integer estoque, BigDecimal preco) {
        this.nome = nome;
        this.marca = marca;
        this.categoria = categoria;
        this.estoque = estoque;
        this.preco = preco;
    }

    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    /** True quando existe uma foto cadastrada. */
    public boolean isTemFoto() {
        return url != null && !url.isBlank();
    }
}