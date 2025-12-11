package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.DatosCompra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatosCompraRepo extends JpaRepository<DatosCompra, Integer> {
}
