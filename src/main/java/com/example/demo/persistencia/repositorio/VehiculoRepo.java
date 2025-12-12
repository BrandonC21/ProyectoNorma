package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepo extends JpaRepository<Vehiculo,Integer> {
    List<Vehiculo> findByVendidoFalse();
    List<Vehiculo> findByMarcaAndVendidoFalse(String marca);

}
