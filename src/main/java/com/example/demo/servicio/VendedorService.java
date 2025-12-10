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

    // 1. Guardar solo el vendedor (Primer paso)
    public Vendedor registrarVendedor(Vendedor vendedor) {
        return vendedorRepo.save(vendedor);
    }

    // Agregar o modificar datos bancarios

    public Vendedor guardarDatosBancarios(int vendedorId, DatosBancarios nuevosDatosBancarios) throws Exception {

        // 1. Obtener el Vendedor
        Vendedor vendedor = vendedorRepo.findById(vendedorId)
                .orElseThrow(() -> new Exception("Vendedor no encontrado"));

        // 2. Intentar encontrar los datos bancarios existentes para este vendedor.
        Optional<DatosBancarios> datosExistentesOpt = datosBancariosRepo.findByVendedorId(vendedorId);

        DatosBancarios datosAActualizar;

        if (datosExistentesOpt.isPresent()) {
            // *** CASO 1: ACTUALIZACIÓN (UPDATE) ***
            datosAActualizar = datosExistentesOpt.get();

            // Mapear los nuevos datos al objeto existente
            datosAActualizar.setNombreTitular(nuevosDatosBancarios.getNombreTitular());
            datosAActualizar.setCLABE(nuevosDatosBancarios.getCLABE());
            datosAActualizar.setNombreBanco(nuevosDatosBancarios.getNombreBanco());
            // ... setear los demás campos bancarios que apliquen ...

            // El ID del vendedor y la clave primaria (PK) del objeto ya están seteados.
            //

        } else {
            // *** CASO 2: INSERCIÓN (INSERT) ***
            datosAActualizar = nuevosDatosBancarios;
            // La entidad ya está lista para ser insertada, solo falta vincular el vendedor
        }

        // 3. Establecer/Re-establecer la relación bidireccional (Crucial para persistencia)
        datosAActualizar.setVendedor(vendedor);

        // 4. Persistir (Hibernate decidirá si es UPDATE o INSERT)
        DatosBancarios datosPersistidos = datosBancariosRepo.save(datosAActualizar);

        // 5. Opcional, pero recomendado: Asegurar la consistencia bidireccional en Vendedor
        vendedor.setDatosBancarios(datosPersistidos);
        // Si la relación es bidireccional, es bueno guardar también el vendedor para asegurar
        // que la referencia en la entidad Vendedor esté actualizada en la sesión.
        // vendedorRepo.save(vendedor);

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
