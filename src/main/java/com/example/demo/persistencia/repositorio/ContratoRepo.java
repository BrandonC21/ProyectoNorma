package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepo extends JpaRepository<Contrato, Integer> {
    List<Contrato> findByVersionAcuerdoAceptada(String version);

    List<Contrato> findByClienteId(int clienteId);
}
