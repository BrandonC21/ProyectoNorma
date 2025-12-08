package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepo extends JpaRepository<Cliente,Integer> {
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
