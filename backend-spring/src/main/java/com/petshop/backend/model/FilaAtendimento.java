package com.petshop.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Fila de atendimento persistida no banco (tabela fila_atendimento).
 *
 * <p>A tabela real do banco possui estas colunas:
 * id_atendimento, criticidade, data_hora_entrada, status,
 * metodo_cancelamento, id_servicoFK, id_petFK, id_funcionarioFK, data_hora.</p>
 *
 * <p>O nome do cliente e do pet nao ficam gravados aqui: sao obtidos
 * por JOIN com as tabelas pet e tutor na hora de montar a resposta.</p>
 */
@Entity
@Table(name = "fila_atendimento")
public class FilaAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atendimento")
    private Integer idAtendimento;

    /** Prioridade do atendimento: Normal, Urgente, etc. */
    @Column(name = "criticidade")
    private String criticidade;

    @Column(name = "data_hora_entrada")
    private LocalDateTime dataHoraEntrada;

    /** Em espera | Em atendimento | Concluído | Cancelado */
    @Column(name = "status")
    private String status;

    /** Motivo do cancelamento (RF05 exige justificativa). */
    @Column(name = "metodo_cancelamento")
    private String metodoCancelamento;

    @Column(name = "id_servicoFK")
    private Integer idServico;

    @Column(name = "id_petFK")
    private Integer idPet;

    @Column(name = "id_funcionarioFK")
    private Integer idFuncionario;

    /** Coluna legada, mantida no banco mas nao usada. */
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    public FilaAtendimento() {}

    // ---------------------------------------------------------------
    // Getters e Setters
    // ---------------------------------------------------------------

    public Integer getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Integer idAtendimento) {
        this.idAtendimento = idAtendimento;
    }

    public String getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(String criticidade) {
        this.criticidade = criticidade;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMetodoCancelamento() {
        return metodoCancelamento;
    }

    public void setMetodoCancelamento(String metodoCancelamento) {
        this.metodoCancelamento = metodoCancelamento;
    }

    public Integer getIdServico() {
        return idServico;
    }

    public void setIdServico(Integer idServico) {
        this.idServico = idServico;
    }

    public Integer getIdPet() {
        return idPet;
    }

    public void setIdPet(Integer idPet) {
        this.idPet = idPet;
    }

    public Integer getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(Integer idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
