package com.petshop.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Integer idAval;

    @Column(name = "servico_id")
    private Integer servicoId;

    @Column(name = "nome_pet")
    private String nomePet;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "nota")
    private Integer nota;

    @Column(name = "aval_data")
    private LocalDateTime avalData;

    @Column(name = "comentario")
    private String comentario;

    @Column(name = "respondida")
    private Boolean respondida = false;

    public Avaliacao() {}

    public Avaliacao(Integer servicoId, String nomePet, String descricao, Integer nota, LocalDateTime avalData, String comentario, Boolean respondida) {
        this.servicoId = servicoId;
        this.nomePet = nomePet;
        this.descricao = descricao;
        this.nota = nota;
        this.avalData = avalData;
        this.comentario = comentario;
        this.respondida = respondida;
    }

    public Integer getIdAval() { return idAval; }
    public void setIdAval(Integer idAval) { this.idAval = idAval; }

    public Integer getServicoId() { return servicoId; }
    public void setServicoId(Integer servicoId) { this.servicoId = servicoId; }

    public String getNomePet() { return nomePet; }
    public void setNomePet(String nomePet) { this.nomePet = nomePet; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }

    public LocalDateTime getAvalData() { return avalData; }
    public void setAvalData(LocalDateTime avalData) { this.avalData = avalData; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Boolean getRespondida() { return respondida; }
    public void setRespondida(Boolean respondida) { this.respondida = respondida; }
}