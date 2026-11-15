package com.petshop.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tutor")
public class Cliente {

    @Id
    @Column(name = "cpf", length = 20)
    private String cpf;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telefone", length = 50)
    private String telefone;

    @Column(name = "endereco", length = 255)
    private String endereco;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "senha", length = 255)
    private String senha;



    public Cliente() {}

    public Cliente(String cpf, String nome, String email, String telefone, String endereco, String usuario, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }

    // Na tabela tutor o "documento" e o proprio CPF
    public String getNDocumento() { return cpf; }
    public void setSenha(String senha) { this.senha = senha; }
}