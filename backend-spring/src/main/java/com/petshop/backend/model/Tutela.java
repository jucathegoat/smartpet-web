package com.petshop.backend.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Vinculo pet <-> tutor (tabela `tutela`).
 * A PK e composta: (codigo_pet, cpf).
 */
@Entity
@Table(name = "tutela")
@IdClass(Tutela.TutelaId.class)
public class Tutela {

    @Id
    @Column(name = "codigo_pet")
    private Integer codigoPet;

    @Id
    @Column(name = "cpf", length = 12)
    private String cpf;

    public Tutela() {}

    public Integer getCodigoPet() { return codigoPet; }
    public void setCodigoPet(Integer codigoPet) { this.codigoPet = codigoPet; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    /** Chave composta da tabela. */
    public static class TutelaId implements Serializable {
        private Integer codigoPet;
        private String cpf;

        public TutelaId() {}

        public TutelaId(Integer codigoPet, String cpf) {
            this.codigoPet = codigoPet;
            this.cpf = cpf;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TutelaId)) return false;
            TutelaId that = (TutelaId) o;
            return Objects.equals(codigoPet, that.codigoPet) && Objects.equals(cpf, that.cpf);
        }

        @Override
        public int hashCode() { return Objects.hash(codigoPet, cpf); }
    }
}
