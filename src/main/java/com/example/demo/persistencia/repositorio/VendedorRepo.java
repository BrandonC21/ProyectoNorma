package com.example.demo.persistencia.repositorio;


import com.example.demo.persistencia.entidades.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendedorRepo extends JpaRepository<Vendedor, Integer> {
    Optional<Vendedor> findAllByRFC(String rfc);

}
