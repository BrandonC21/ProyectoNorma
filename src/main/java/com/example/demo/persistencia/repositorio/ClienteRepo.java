package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepo extends JpaRepository<Cliente,Integer> {
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    Optional<Cliente> findAllByRFC(String rfc);
}
