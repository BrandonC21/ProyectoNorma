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

    // 2. Guardar los datos bancarios y vincularlos al vendedor (Tercer paso)
    public Vendedor guardarDatosBancarios(int vendedorId, DatosBancarios datosBancarios) throws Exception {
        Vendedor vendedor = vendedorRepo.findById(vendedorId)
                .orElseThrow(() -> new Exception("Vendedor no encontrado"));

        datosBancarios.setVendedor(vendedor);
        datosBancariosRepo.save(datosBancarios);

        // Asegurarse de que la relación bidireccional se establezca si es necesario
        // vendedor.setDatosBancarios(datosBancarios);
        // return vendedorRepo.save(vendedor);

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
