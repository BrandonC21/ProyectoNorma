package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.DatosBancarios;
import com.example.demo.persistencia.entidades.Vendedor;
import com.example.demo.persistencia.repositorio.DatosBancariosRepo;
import com.example.demo.persistencia.repositorio.VendedorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendedorService {
    @Autowired
    private VendedorRepo vendedorRepo;
    @Autowired
    private DatosBancariosRepo datosBancariosRepo;

    // Registra el vendedro
    public Vendedor registrarVendedor(Vendedor vendedor) {
        return vendedorRepo.save(vendedor);
    }

    // Agregar o modificar datos bancarios

    public Vendedor guardarDatosBancarios(int vendedorId, DatosBancarios nuevosDatosBancarios) throws Exception {

        // Se obtiene el vendedor
        Vendedor vendedor = vendedorRepo.findById(vendedorId)
                .orElseThrow(() -> new Exception("Vendedor no encontrado"));

        // Busca los datos bancarios para un usuario
        Optional<DatosBancarios> datosExistentesOpt = datosBancariosRepo.findByVendedorId(vendedorId);

        DatosBancarios datosAActualizar;

        if (datosExistentesOpt.isPresent()) {

            datosAActualizar = datosExistentesOpt.get();
            datosAActualizar.setNombreTitular(nuevosDatosBancarios.getNombreTitular());
            datosAActualizar.setCLABE(nuevosDatosBancarios.getCLABE());
            datosAActualizar.setNombreBanco(nuevosDatosBancarios.getNombreBanco());

        } else {
            // Realiza la insercion
            datosAActualizar = nuevosDatosBancarios;

        }
        // Establecer la relacion biderecional
        datosAActualizar.setVendedor(vendedor);
        DatosBancarios datosPersistidos = datosBancariosRepo.save(datosAActualizar);
        vendedor.setDatosBancarios(datosPersistidos);

        return vendedor;
    }

    public Vendedor buscarVendedorRegistrado(Vendedor vendedor) {
        Optional<Vendedor> vendedorExistente = vendedorRepo.findAllByRFC(vendedor.getRFC());
        if (vendedorExistente.isPresent()){
            System.out.println("Vendedor ya registrado por RFC. Obteniendo ID existente.");
            return vendedorExistente.get();
        } else{
            return registrarVendedor(vendedor);
        }
    }

}
