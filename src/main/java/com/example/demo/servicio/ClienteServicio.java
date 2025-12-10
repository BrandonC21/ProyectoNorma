package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.Cliente;
import com.example.demo.persistencia.repositorio.ClienteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteServicio {
    @Autowired
    private ClienteRepo clienteRepo;

    public Cliente registrarCliente(Cliente cliente){
        cliente.setRFC(CifradoUtil.cifrar(cliente.getRFC()));
        cliente.setDireccion(CifradoUtil.cifrar(cliente.getDireccion()));
        return clienteRepo.save(cliente);
    }

    public Cliente obtenerCliente(int id){
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // DESCIFRADO (Temporal para mostrar en el Contrato antes de la firma)
        cliente.setRFC(CifradoUtil.descifrar(cliente.getRFC()));
        cliente.setDireccion(CifradoUtil.descifrar(cliente.getDireccion()));
        return cliente;
    }

    public Cliente clienteRegistrado(Cliente cliente) {
        String rfcCifrado = CifradoUtil.cifrar(cliente.getRFC());
        Optional<Cliente> clienteExixtente = clienteRepo.findAllByRFC(rfcCifrado);
        if (clienteExixtente.isPresent()){
            //Actulizar solo los campos
            Cliente actualizado = clienteExixtente.get();
            actualizado.setCorreo(cliente.getCorreo());
            actualizado.setTelefono(cliente.getTelefono());
            actualizado.setIngresoMensual(cliente.getIngresoMensual());
            return clienteRepo.save(actualizado);
        } else {
            return registrarCliente(cliente);
        }
    }

}
