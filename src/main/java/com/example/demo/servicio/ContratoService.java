package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.Cliente;
import com.example.demo.persistencia.entidades.Contrato;
import com.example.demo.persistencia.entidades.Vehiculo;
import com.example.demo.persistencia.repositorio.ClienteRepo;
import com.example.demo.persistencia.repositorio.ContratoRepo;
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
    public static final String VERSION_ACTUAL_ACUERDO = "V1.5"; // Versión para Click-Wrap

    /**
     * Genera el contrato, valida la versión del acuerdo (Click-Wrap) y lo persiste.
     */
    public Contrato generarContrato(int clienteId, int vehiculoId, String versionAceptada) {

        // 1. Validación de Licencia Click-Wrap
        if (!VERSION_ACTUAL_ACUERDO.equals(versionAceptada)) {
            // Este error previene la creación del contrato si el cliente no aceptó la última versión.
            throw new IllegalStateException("Error legal: Debe aceptar la versión " + VERSION_ACTUAL_ACUERDO + " del acuerdo.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        Contrato contrato = new Contrato();
        contrato.setCliente(cliente);
        contrato.setVehiculo(vehiculo);
        contrato.setFechaCelebracion(LocalDate.now());

        // 2. Registro de Cumplimiento Legal (usa la propiedad corregida)
        contrato.setVersionAcuerdoAceptada(versionAceptada);

        return contratoRepository.save(contrato);
    }

}
