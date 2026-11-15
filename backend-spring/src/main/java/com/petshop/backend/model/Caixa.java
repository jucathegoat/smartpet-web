package com.petshop.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Turno de caixa: abre com um saldo, recebe entradas e saidas, fecha com o saldo final.
 * Tabela real: caixa
 */
@Entity
@Table(name = "caixa")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caixa")
    private Integer idCaixa;

    @Column(name = "saldo_inicial", precision = 10, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_final", precision = 10, scale = 2)
    private BigDecimal saldoFinal;

    @Column(name = "horario_abertura")
    private LocalDateTime horarioAbertura;

    @Column(name = "horario_fechamento")
    private LocalDateTime horarioFechamento;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "id_funcionarioFK")
    private Integer idFuncionario;

    public Caixa() {}

    public Caixa(BigDecimal saldoInicial, LocalDateTime horarioAbertura) {
        this.saldoInicial = saldoInicial;
        this.horarioAbertura = horarioAbertura;
    }

    /** Caixa aberto = ainda nao foi fechado. */
    public boolean isAberto() {
        return horarioFechamento == null;
    }

    public Integer getIdCaixa() { return idCaixa; }
    public void setIdCaixa(Integer idCaixa) { this.idCaixa = idCaixa; }

    public BigDecimal getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(BigDecimal saldoInicial) { this.saldoInicial = saldoInicial; }

    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }

    public LocalDateTime getHorarioAbertura() { return horarioAbertura; }
    public void setHorarioAbertura(LocalDateTime horarioAbertura) { this.horarioAbertura = horarioAbertura; }

    public LocalDateTime getHorarioFechamento() { return horarioFechamento; }
    public void setHorarioFechamento(LocalDateTime horarioFechamento) { this.horarioFechamento = horarioFechamento; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public Integer getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(Integer idFuncionario) { this.idFuncionario = idFuncionario; }
}
