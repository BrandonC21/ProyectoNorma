package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.DatosCompra;
import com.example.demo.persistencia.repositorio.DatosCompraRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatosCompraServicio {
    @Autowired
    private DatosCompraRepo datosCompraRepo;

    public DatosCompra agregarDatos(DatosCompra datos){
        return datosCompraRepo.save(datos);
    }
}
