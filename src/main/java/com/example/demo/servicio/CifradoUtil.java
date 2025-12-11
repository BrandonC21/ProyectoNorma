package com.example.demo.servicio;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CifradoUtil {

    // Clave fija de 16 caracteres (AES-128)
    private static final String CLAVE = "1234567890123456";

    //  CORRECCIÓN CRÍTICA: Especificar el algoritmo, modo y relleno.
    // Usamos ECB y PKCS5Padding para forzar la consistencia.
    private static final String MODO_CIFRADO = "AES/ECB/PKCS5Padding";

    // Método para cifrar datos sensibles antes de guardarlos en la BD
    public static String cifrar(String dato) {
        try {
            if (dato == null || dato.isEmpty()) return "";

            SecretKeySpec key = new SecretKeySpec(CLAVE.getBytes(), "AES");

            // 🚩 CAMBIO: Usamos la especificación completa
            Cipher cipher = Cipher.getInstance(MODO_CIFRADO);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] cifrado = cipher.doFinal(dato.getBytes());

            return Base64.getEncoder().encodeToString(cifrado);

        } catch (Exception e) {
            throw new RuntimeException("Error cifrando: " + e.getMessage());
        }
    }

    // Metodo para descifrar datos
    public static String descifrar(String datoCifrado) {
        try {
            if (datoCifrado == null || datoCifrado.isEmpty()) return "";

            SecretKeySpec key = new SecretKeySpec(CLAVE.getBytes(), "AES");

            // 🚩 CAMBIO: Usamos la especificación completa
            Cipher cipher = Cipher.getInstance(MODO_CIFRADO);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodificado = Base64.getDecoder().decode(datoCifrado);
            byte[] decifrado = cipher.doFinal(decodificado);

            return new String(decifrado);

        } catch (Exception e) {
            throw new RuntimeException("Error descifrando: " + e.getMessage());
        }
    }
}