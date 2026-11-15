package com.petshop.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma entrada de dinheiro no caixa (servico avulso, venda sem nota, etc.).
 * Tabela real: receita
 */
@Entity
@Table(name = "receita")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receita")
    private Integer idReceita;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "id_caixaFK")
    private Integer idCaixa;

    public Receita() {}

    public Receita(String categoria, String descricao, BigDecimal valor, LocalDateTime dataHora, Integer idCaixa) {
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataHora = dataHora;
        this.idCaixa = idCaixa;
    }

    public Integer getIdReceita() { return idReceita; }
    public void setIdReceita(Integer idReceita) { this.idReceita = idReceita; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Integer getIdCaixa() { return idCaixa; }
    public void setIdCaixa(Integer idCaixa) { this.idCaixa = idCaixa; }
}
