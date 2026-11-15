package com.petshop.backend.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemFilaDTO {
    private Integer id;
    private String nomeCliente;
    private String nomePet;
    private String servico;
    private LocalDateTime chegada;
    private String status;
    private String statusClass;
    private String motivoCancelamento;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public ItemFilaDTO(Integer id, String nomeCliente, String nomePet, String servico) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.nomePet = nomePet;
        this.servico = servico;
        this.chegada = LocalDateTime.now();
        this.status = "Em espera";
        this.statusClass = "badge-espera";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomePet() {
        return nomePet;
    }

    public void setNomePet(String nomePet) {
        this.nomePet = nomePet;
    }

    public String getServico() {
        return servico;
    }

    public void setServico(String servico) {
        this.servico = servico;
    }

    public LocalDateTime getChegada() {
        return chegada;
    }

    public String getHorarioChegada() {
        return chegada != null ? chegada.format(FORMATO_HORA) : "";
    }

    public long getMinutosEspera() {
        if (chegada == null) return 0;
        return Duration.between(chegada, LocalDateTime.now()).toMinutes();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("Em atendimento".equalsIgnoreCase(status)) {
            this.statusClass = "badge-atendimento";
        } else if ("Cancelado".equalsIgnoreCase(status)) {
            this.statusClass = "badge-cancelado";
        } else if ("Concluído".equalsIgnoreCase(status) || "Concluido".equalsIgnoreCase(status)) {
            this.statusClass = "badge-concluido";
        } else {
            this.statusClass = "badge-espera";
        }
    }

    public String getStatusClass() {
        return statusClass;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }
}
