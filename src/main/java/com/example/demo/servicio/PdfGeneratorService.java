package com.example.demo.servicio;

import com.example.demo.persistencia.entidades.Contrato;
import com.example.demo.persistencia.entidades.Cliente;
import com.example.demo.persistencia.entidades.Vehiculo;
import com.example.demo.persistencia.entidades.Vendedor;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    // Dirección fija de la empresa
    private static final String DIRECCION_EMPRESA = "Av. San Lorenzo  #742, Col. Lomas de San Lorenzo, C.P. 55000, CDMX.";

    // Formateador para fecha
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generarContratoPDF(Contrato contrato) throws IOException {

        // Creamos el documento y el flujo de salida en memoria
        Document document = new Document(PageSize.A4, 50, 50, 50, 50); // Márgenes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Definición de fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(40, 167, 69)); // Verde
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(0, 64, 128)); // Azul oscuro
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // --- TÍTULO ---
            Paragraph title = new Paragraph("CONTRATO DE COMPRAVENTA DE VEHÍCULO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            document.add(new Paragraph("Fecha de Contrato: " + contrato.getFechaCelebracion().format(DATE_FORMATTER), normalFont));
            document.add(Chunk.NEWLINE);

            // --- SECCIÓN 1: DATOS DEL COMPRADOR ---
            document.add(new Paragraph("1. DATOS DEL CLIENTE COMPRADOR", headerFont));
            document.add(new Paragraph("Nombre: " + contrato.getCliente().getNombre() + " " + contrato.getCliente().getApellidoP() + " " + contrato.getCliente().getApellidoM(), normalFont));
            document.add(new Paragraph("RFC: " + contrato.getCliente().getRFC(), normalFont));
            document.add(new Paragraph("Dirección: " + contrato.getCliente().getDireccion(), normalFont));
            document.add(new Paragraph("Teléfono: " + contrato.getCliente().getTelefono(), normalFont));
            document.add(Chunk.NEWLINE);

            // --- SECCIÓN 2: DATOS DEL VEHÍCULO ---
            Vehiculo vehiculo = contrato.getVehiculo();
            document.add(new Paragraph("2. DATOS DEL VEHÍCULO VENDIDO", headerFont));
            document.add(new Paragraph("Marca: " + vehiculo.getMarca(), normalFont));
            document.add(new Paragraph("Modelo: " + vehiculo.getModelo(), normalFont));
            document.add(new Paragraph("Año de Fabricación: " + vehiculo.getAnioFabricacion(), normalFont));
            document.add(new Paragraph("VIN/Número de Serie: " + vehiculo.getNumeroSerie(), normalFont));
            document.add(new Paragraph("Color del Vehiculo: " + vehiculo.getColor(),normalFont ));
            document.add(new Paragraph("Precio de Venta Final: $" + vehiculo.getPrecio(), normalFont));
            document.add(Chunk.NEWLINE);

            // --- SECCIÓN 3: DATOS BANCARIOS (DESTINO DEL PAGO) ---
            // Asumo que tienes una entidad o campos con la info bancaria de la empresa/vendedor
            Vendedor vendedor = contrato.getVehiculo().getVendedor().getDatosBancarios().getVendedor();
            document.add(new Paragraph("3. INFORMACIÓN BANCARIA PARA PAGO", headerFont));
            document.add(new Paragraph("Nombre del Titular: "+ vendedor.getDatosBancarios().getNombreTitular(), normalFont));
            document.add(new Paragraph("Nombre de Banco: "+vendedor.getDatosBancarios().getNombreBanco(), normalFont));
            document.add(new Paragraph("CLABE Interbancaria: "+vendedor.getDatosBancarios().getCLABE(), normalFont));
            document.add(new Paragraph("Concepto de Pago: Compra Vehículo " + vehiculo.getId(), normalFont));
            document.add(Chunk.NEWLINE);

            // --- SECCIÓN 4: ACUERDO DE ENTREGA Y RECOLECCIÓN ---
            document.add(new Paragraph("4. ACUERDO DE ENTREGA Y RECOLECCIÓN", headerFont));
            document.add(new Paragraph("El vehículo será recogido por el comprador en la siguiente dirección:", normalFont));
            document.add(new Paragraph("Dirección de la Empresa: " + DIRECCION_EMPRESA, normalFont));
            document.add(new Paragraph("Fecha Acordada de Entrega: " + contrato.getFechaCelebracion().format(DATE_FORMATTER), normalFont));
            //document.add(new Paragraph("Hora Acordada de Entrega: " + "12.00  a  16.30");
            document.add(Chunk.NEWLINE);

            // --- SECCIÓN 5: CLÁUSULAS LEGALES (Resumen) ---
            document.add(new Paragraph("5. CLÁUSULAS LEGALES", headerFont));
            document.add(new Paragraph("1. Objeto: El presente contrato tiene por objeto la compraventa del vehículo descrito anteriormente.", normalFont));
            document.add(new Paragraph("2. Estado del Vehículo: El comprador declara haber inspeccionado el vehículo y aceptarlo en su estado actual. El vendedor garantiza que el vehículo no cuenta con adeudos, gravámenes o reporte de robo.", normalFont));
            document.add(new Paragraph("3. Pago: El comprador pagará el precio pactado en los términos acordados, considerando como válido únicamente el pago reflejado en la cuenta del vendedor.", normalFont));
            document.add(new Paragraph("4. Entrega: La entrega se llevará a cabo en la dirección indicada, liberando al vendedor de cualquier responsabilidad posterior por uso, daños o infracciones.", normalFont));
            document.add(new Paragraph("5. Transmisión de Propiedad: La propiedad se transfiere al comprador únicamente al realizar el pago total.", normalFont));
            document.add(new Paragraph("6. Responsabilidad: Cualquier multa o daño generado después de la entrega será responsabilidad del comprador.", normalFont));
            document.add(new Paragraph("7. Protección de Datos: Los datos personales serán tratados conforme al Aviso de Privacidad vigente.", normalFont));
            document.add(new Paragraph("8. Cancelación: El anticipo del 10% no es reembolsable salvo que el vendedor cancele la operación.", normalFont));
            document.add(new Paragraph("9. Jurisdicción: Ambas partes se someten a las leyes y tribunales de la Ciudad de México para la interpretación del contrato.", normalFont));
            document.add(new Paragraph("10. Problemas Legales: Si el Vehiculo presenta anomalias no detectadas al momento de hacer la compra, la responsabilidad es completa del vendedor.", normalFont));


            // --- FIRMAS ---
            document.add(new Paragraph("________________________________", normalFont));
            document.add(new Paragraph("Firma del Comprador", normalFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("________________________________", normalFont));
            document.add(new Paragraph("Firma del Representante Legal (Vendedor)", normalFont));

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Error al generar el PDF: " + e.getMessage());
        }

        return outputStream.toByteArray();
    }
}