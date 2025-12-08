package com.example.demo.persistencia.entidades;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "contratos")
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Column(name = "fecha_celebracion")
    private LocalDate fechaCelebracion;

    // EL CAMPO CLAVE (asegúrate de que esté escrito EXACTAMENTE así):
    @Column(name = "version_acuerdo_aceptada")
    private String versionAcuerdoAceptada;

    // --- IMPORTANTE: Getters y Setters ---

    // Getter que debe existir para que Spring Data JPA lo reconozca
    public String getVersionAcuerdoAceptada() {
        return versionAcuerdoAceptada;
    }

    // Setter
    public void setVersionAcuerdoAceptada(String versionAcuerdoAceptada) {
        this.versionAcuerdoAceptada = versionAcuerdoAceptada;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public LocalDate getFechaCelebracion() {
        return fechaCelebracion;
    }

    public void setFechaCelebracion(LocalDate fechaCelebracion) {
        this.fechaCelebracion = fechaCelebracion;
    }


}
