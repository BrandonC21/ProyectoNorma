package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.Cliente;
import com.example.demo.persistencia.entidades.Contrato;
import com.example.demo.persistencia.entidades.DatosCompra;
import com.example.demo.persistencia.entidades.Vehiculo;
import com.example.demo.persistencia.repositorio.ClienteRepo;
import com.example.demo.persistencia.repositorio.ContratoRepo;
import com.example.demo.persistencia.repositorio.DatosCompraRepo;
import com.example.demo.persistencia.repositorio.VehiculoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ContratoService {
    @Autowired
    private ContratoRepo contratoRepository;
    @Autowired
    private ClienteRepo clienteRepository;
    @Autowired
    private VehiculoRepo vehiculoRepository;
    @Autowired
    private DatosCompraRepo datosCompraRepo;


    public static final String VERSION_ACTUAL_ACUERDO = "V1.5"; // Versión para Click-Wrap

    public Contrato generarContrato(int clienteId, int vehiculoId, int datosId,String versionAceptada) {

        // 1. Validación de Licencia Click-Wrap
        if (!VERSION_ACTUAL_ACUERDO.equals(versionAceptada)) {
            throw new IllegalStateException("Error legal: Debe aceptar la versión " + VERSION_ACTUAL_ACUERDO + " del acuerdo.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
        DatosCompra datos = datosCompraRepo.findById(datosId)
                .orElseThrow(() -> new RuntimeException("Datos Bancarios no encontrados"));

        Contrato contrato = new Contrato();
        contrato.setCliente(cliente);
        contrato.setVehiculo(vehiculo);
        contrato.setMetodoPagoUsado(datos);
        contrato.setFechaCelebracion(LocalDate.now());

        //Cumplimiento del clip-Wrap para poder genera los datos del contrato
        contrato.setVersionAcuerdoAceptada(versionAceptada);

        return contratoRepository.save(contrato);
    }
    //Obtiene un contrato mediante el id
    public Contrato obtenerContratoPorId(int id){
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        return contrato;
    }

}
