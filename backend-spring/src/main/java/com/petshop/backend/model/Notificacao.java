package com.petshop.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
// Placeholder: a tabela "notificacao" ainda nao existe no banco real.
// O Spring cria esta tabela automaticamente na primeira execucao.
@Table(name = "smartpet_notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "mensagem", nullable = false, length = 1000)
    private String mensagem;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "lida")
    private Boolean lida = false;

    public Notificacao() {}

    public Notificacao(String titulo, String mensagem, LocalDateTime dataHora) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
        this.lida = false;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
}