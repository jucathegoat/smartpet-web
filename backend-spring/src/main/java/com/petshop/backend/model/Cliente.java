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

    /** ativo | Bloqueado (RF03) */
    @Column(name = "status_conta", length = 20)
    private String statusConta;



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

    public String getStatusConta() { return statusConta; }
    public void setStatusConta(String statusConta) { this.statusConta = statusConta; }

    /**
     * RF03 — cliente bloqueado nao consegue entrar no app.
     * O status e gravado como "Bloqueado: motivo", por isso usamos startsWith.
     */
    public boolean isBloqueado() {
        return statusConta != null
                && statusConta.trim().toLowerCase().startsWith("bloqueado");
    }

    /**
     * RF03 — motivo informado no bloqueio.
     *
     * <p>O banco define status_conta como varchar(20) e o schema nao pode ser
     * alterado, por isso o motivo NAO e gravado dentro da coluna. A coluna
     * guarda apenas "Bloqueado" e o motivo fica neste mapa em memoria.</p>
     */
    private static final java.util.Map<String, String> MOTIVOS =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static void registrarMotivo(String cpf, String motivo) {
        if (cpf == null) return;
        if (motivo == null || motivo.isBlank()) {
            MOTIVOS.remove(cpf);
        } else {
            MOTIVOS.put(cpf, motivo.trim());
        }
    }

    public String getMotivoBloqueio() {
        if (!isBloqueado()) return null;
        String guardado = MOTIVOS.get(this.cpf);
        return guardado != null ? guardado : "não informado";
    }
}