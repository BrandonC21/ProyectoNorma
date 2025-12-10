package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.DatosBancarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatosBancariosRepo extends JpaRepository<DatosBancarios,Integer> {
    Optional<DatosBancarios> findByVendedorId(int vendedorid);
}
