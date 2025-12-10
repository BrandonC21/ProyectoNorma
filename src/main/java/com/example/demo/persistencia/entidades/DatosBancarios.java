package com.example.demo.persistencia.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "datos_bancarios")
public class DatosBancarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // CRÍTICO: Clave Bancaria Estandarizada
    @Column(nullable = false, unique = true)
    private String CLABE;

    @Column(nullable = false)
    private String nombreBanco;

    @Column(nullable = false)
    private String nombreTitular;

    // Relación OneToOne con Vendedor
    @OneToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCLABE() {
        return CLABE;
    }

    public void setCLABE(String CLABE) {
        this.CLABE = CLABE;
    }

    public String getNombreBanco() {
        return nombreBanco;
    }

    public void setNombreBanco(String nombreBanco) {
        this.nombreBanco = nombreBanco;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }
}
