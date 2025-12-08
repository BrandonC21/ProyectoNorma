package com.example.demo.servicio;

public class CifradoUtil {
    // Método para cifrar datos sensibles antes de guardarlos en la BD
    public static String cifrar(String dato) {
        // En producción, se usaría un algoritmo robusto como AES o RSA.
        if (dato == null || dato.isEmpty()) return "";
        return "CIFRADO-LDPP-" + dato.hashCode() + "-" + dato.substring(0, Math.min(3, dato.length()));
    }

    // Método para descifrar datos (necesario para la vista de acuerdo)
    public static String descifrar(String datoCifrado) {
        // Simulación: extraemos parte del hash para demostrar el proceso
        if (datoCifrado == null || !datoCifrado.startsWith("CIFRADO-LDPP-")) return datoCifrado;

        // Simplemente devolveremos una representación para la demostración
        return "DESCIFRADO_OK";
    }
}
