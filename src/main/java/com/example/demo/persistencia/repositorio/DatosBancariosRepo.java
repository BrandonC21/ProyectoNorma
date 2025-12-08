package com.example.demo.persistencia.repositorio;

import com.example.demo.persistencia.entidades.DatosBancarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatosBancariosRepo extends JpaRepository<DatosBancarios,Integer> {

}
