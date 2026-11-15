package com.petshop.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma saida de dinheiro no caixa ((racao, luz, salario, aluguel...)).
 * Tabela real: despesa
 */
@Entity
@Table(name = "despesa")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_despesa")
    private Integer idDespesa;

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

    public Despesa() {}

    public Despesa(String categoria, String descricao, BigDecimal valor, LocalDateTime dataHora, Integer idCaixa) {
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataHora = dataHora;
        this.idCaixa = idCaixa;
    }

    public Integer getIdDespesa() { return idDespesa; }
    public void setIdDespesa(Integer idDespesa) { this.idDespesa = idDespesa; }

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
