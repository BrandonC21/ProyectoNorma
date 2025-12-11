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
        return clienteRepo.save(cliente);
    }

    public Cliente obtenerCliente(int id){
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return cliente;
    }

    public Cliente clienteRegistrado(Cliente cliente) {
        String rfcBusqueda = cliente.getRFC();
        Optional<Cliente> clienteExistenteOpt = clienteRepo.findAllByRFC(rfcBusqueda);

        if (clienteExistenteOpt.isPresent()){
            // Cliente existente, actualizar solo los campos especificados
            Cliente actualizado = clienteExistenteOpt.get();
            actualizado.setCorreo(cliente.getCorreo());
            actualizado.setTelefono(cliente.getTelefono());
            actualizado.setIngresoMensual(cliente.getIngresoMensual());

            // Se guardan los cambios (hace un UPDATE)
            Cliente clienteGuardado = clienteRepo.save(actualizado);
            return clienteGuardado;
        } else {
            // Cliente nuevo, registrarlo
            return registrarCliente(cliente);
        }
    }
}