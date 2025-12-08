package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.Vehiculo;
import com.example.demo.persistencia.repositorio.VehiculoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiculoService {
    @Autowired
    private VehiculoRepo vehiculoRepo;
    @Autowired
    private IUploadFileService uploadFileService;

    //Agregar Vehiculo
    public Vehiculo agregarVehiculo(Vehiculo vehiculo) {
        return vehiculoRepo.save(vehiculo);
    }

    //Actulizar el estado del vehiculo
    public void marcarComoVendido(int id) throws Exception{
        Vehiculo vehiculo = vehiculoRepo.findById(id)
                .orElseThrow(() -> new Exception("Vehículo con ID " + id + " no encontrado."));

        vehiculo.setVendido(true); // Se marca como vendido
        vehiculoRepo.save(vehiculo); // Se guarda el cambio (UPDATE)
    }
    //Eliminar Vehiculo
    public void eliminarVehiculo(int id)throws Exception{
        Vehiculo vehiculo = vehiculoRepo.findById(id).orElse(null);
        if(vehiculo != null){
            String nombreImagen =vehiculo.getUrlImagen();
            if (nombreImagen != null && !nombreImagen.isEmpty()){
                // Llama al servicio de subida de archivos para elimar el archivo fidico
              uploadFileService.delete(nombreImagen);
            }

            //Eliminar Vehiculo de la base de datos
            vehiculoRepo.delete(vehiculo);
        } else {
            // Manejar la excepción si el vehículo no existe
            throw new Exception("Vehículo con ID " + id + " no encontrado.");
        }
    }

    public Double estimarPrecioCorregido(double precioBase, long kilometrosActuales, int anioModelo) {

        double factorDepreciacionPorKm = 0.000005; // 0.0005% por KM

        // Años de uso
        int aniosUso = 2025 - anioModelo;
        if (aniosUso < 0) aniosUso = 0; // evitar valores negativos si el modelo es futuro

        // Kilometraje esperado según el uso (15,000 km por año)
        double kilometrajeEsperado = 15000 * aniosUso;

        // Diferencia entre el kilometraje real y el esperado
        double diferenciaKm = kilometrosActuales - kilometrajeEsperado;

        // Ajuste proporcional al precio base
        double ajuste = diferenciaKm * factorDepreciacionPorKm * precioBase;

        // Si tiene más km de lo esperado → resta valor
        // Si tiene menos km de lo esperado → suma valor
        double precioFinal = precioBase + ajuste;

        // Evitar números negativos
        return precioFinal;
    }




}
