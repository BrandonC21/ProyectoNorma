package com.example.demo;
import com.example.demo.persistencia.entidades.*;
import com.example.demo.persistencia.repositorio.ContratoRepo;
import com.example.demo.persistencia.repositorio.DatosCompraRepo;
import com.example.demo.persistencia.repositorio.VehiculoRepo;
import com.example.demo.servicio.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.util.List;

@Controller
@SessionAttributes({"vehiculo", "vendedorId"})
public class ECommerceController {

    @Autowired
    private VehiculoRepo vehiculoRepository;
    @Autowired
    private ContratoRepo contratoRepository;
    @Autowired
    private ClienteServicio clienteService;
    @Autowired
    private ContratoService contratoService;
    @Autowired
    private VehiculoService vehiculoService;
    @Autowired
    private IUploadFileService uploadFileService;
    @Autowired
    private VendedorService vendedorService;
    @Autowired
    private DatosCompraServicio datosCompraServicio;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;


    // Constante para la clave de la oferta en la sesión
    private static final String SESSION_OFERTA_KEY = "ofertaEstimada";


    @ModelAttribute("vehiculo")
    public Vehiculo vehiculo() {
        return new Vehiculo();
    }
    @ModelAttribute("vendedorId")
    public Integer vendedorId() {
        return null;
    }



    // 1. Catálogo con Filtro (usa findByMarca)
    @GetMapping({"/", "/vehiculos"})
    public String listarVehiculos(@RequestParam(required = false) String marca, Model model) {
        if (marca != null && !marca.isEmpty()) {
            //model.addAttribute("vehiculos", vehiculoRepository.findByMarca(marca));
            // Filtra por marca Y que NO este vendido
            model.addAttribute("vehiculos", vehiculoRepository.findByMarcaAndVendidoFalse(marca));
        } else {
            model.addAttribute("vehiculos", vehiculoRepository.findByVendidoFalse());
        }
        return "catalogo";
    }

    // 2. Detalle del Vehículo
    @GetMapping("/vehiculos/{id}")
    public String verDetalle(@PathVariable int id, Model model) {
        model.addAttribute("vehiculo", vehiculoRepository.findById(id).orElseThrow());
        return "detalle-vehiculo";
    }

    // 3. Formulario de Solicitud (GET)
    @GetMapping("/solicitud/{vehiculoId}")
    public String mostrarFormularioCliente(@PathVariable int vehiculoId, Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("vehiculoId", vehiculoId);
        return "solicitud-datos";
    }

    // 4. Guardar Cliente (POST) - Invoca LDPP Service
    @PostMapping("/solicitud/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, @RequestParam int vehiculoId) {
        Cliente clienteGuardado = clienteService.clienteRegistrado(cliente);
        return "redirect:/pago/iniciar/" + vehiculoId + "/" + clienteGuardado.getId();

    }


    @GetMapping("/pago/iniciar/{vehiculoId}/{clienteId}")
    public String mostrarPago(
            @PathVariable int vehiculoId,
            @PathVariable int clienteId,
            Model model) {

        // 1. Obtener el cliente
        Cliente cliente = clienteService.obtenerCliente(clienteId);

        // 2. Pasar IDs y Cliente
        model.addAttribute("cliente", cliente);
        model.addAttribute("vehiculoId", vehiculoId);

        model.addAttribute("datosCompra", new DatosCompra()); // <-- AGREGADO

        // 3. Devolver la vista de pago
        return "pantalla-pago";
    }
    // ECommerceController.java

    @PostMapping("/pago/confirmar")
    public String confirmarPago(@ModelAttribute DatosCompra datosCompra,

            // IDs que viajan por separado
            @RequestParam("vehiculoId") int vehiculoId,
            @RequestParam("clienteId") int clienteId) {

        // 1. Obtener la entidad Cliente para la relación
        Cliente cliente = clienteService.obtenerCliente(clienteId);

        // 2. VINCULAR el Cliente al objeto de pago
        datosCompra.setCliente(cliente);

        // 3.CAMBIO CLAVE: Guardar usando el SERVICIO (que es responsable de guardar y cifrar/gestionar seguridad)
        DatosCompra pagoGuardado = datosCompraServicio.agregarDatos(datosCompra); // <-- USANDO EL SERVICIO INYECTADO

        // 4. Redirigir al acuerdo final, llevando los tres IDs
        return "redirect:/acuerdo/" + vehiculoId + "/" + clienteId + "/" + pagoGuardado.getId();
    }




    // 5. Vista de Acuerdo (Click-Wrap)
    @GetMapping("/acuerdo/{vehiculoId}/{clienteId}/{pagoId}")
    public String mostrarAcuerdo(
            @PathVariable int vehiculoId,
            @PathVariable int clienteId,
            @PathVariable int pagoId, // Nuevo ID de la URL
            Model model) {

        // Obtener y descifrar cliente
        model.addAttribute("cliente", clienteService.obtenerCliente(clienteId));

        // Obtener vehículo
        model.addAttribute("vehiculo", vehiculoRepository.findById(vehiculoId).orElseThrow());

        // Pasar los IDs al modelo para que sean enviados en el POST de aceptación
        model.addAttribute("pagoId", pagoId);
        model.addAttribute("versionActual", ContratoService.VERSION_ACTUAL_ACUERDO);

        return "acuerdo-terminos";
    }


    // 6. Finalizar Contrato (POST) - Invoca Click-Wrap Service
    @PostMapping("/contrato/finalizar")
    public String finalizarContrato(@RequestParam int vehiculoId,
                                    @RequestParam int clienteId,
                                    @RequestParam int pagoId,
                                    @RequestParam String versionAceptada,
                                    RedirectAttributes flash) {
        try {
            Contrato contrato = contratoService.generarContrato(clienteId, vehiculoId, pagoId, versionAceptada);

            vehiculoService.marcarComoVendido(vehiculoId);
            flash.addFlashAttribute("success", "Contrato de venta finalizado y vehículo retirado del inventario.");
            return "redirect:/confirmacion/" + contrato.getId();
        }catch (Exception e){
            flash.addFlashAttribute("error", "Error al finalizar la venta y contrato: " + e.getMessage());
            return "redirect:/acuerdo/" + vehiculoId + "/" + clienteId + "/" + pagoId;
        }
      }


    // 7. Confirmación de Compra
    @GetMapping("/confirmacion/{contratoId}")
    public String verConfirmacion(@PathVariable int contratoId, Model model) {
        model.addAttribute("contrato", contratoRepository.findById(contratoId).orElseThrow());
        return "confirmacion";
    }

    // 8. Endpoint de Auditoría Administrativa (Usa findByVersionAcuerdoAceptada)
    @GetMapping("/admin/auditoria")
    public String auditarContratosPorVersion(Model model) {
        String versionAuditar = ContratoService.VERSION_ACTUAL_ACUERDO;

        // Uso del método personalizado del repositorio para auditar el cumplimiento legal
        List<Contrato> contratosAuditados = contratoRepository.findByVersionAcuerdoAceptada(versionAuditar);

        model.addAttribute("contratos", contratosAuditados);
        model.addAttribute("version", versionAuditar);
        return "auditoria-contratos";
    }
    // mostrar el formulario de registro de vendedor
    @GetMapping("/vendedor/registrar")
    public String mostrarFormularioVendedror(Model model, SessionStatus status){
        status.setComplete();
        model.addAttribute("vendedor", new Vendedor());
        return "registro-vendedor";
    }


    //9 mostrar formulario de registro de vehiculo
    @GetMapping("/vehiculos/registrar")
    public String mostrarFormularioVehiculo(Model model){
        model.addAttribute("vehiculo", new Vehiculo());
        return "vender-vehiculo";
    }

    //10 Mostrar la imagen en detalle y catalogo
    @GetMapping(value = "/uploads/{filename}")
    public ResponseEntity<Resource> uploadFile(@PathVariable String filename) {
        Resource resource = null;
        try {
            resource = uploadFileService.load(filename);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }



    //Guardar el vendedor
    @PostMapping("/vendedor/save")
    public String guardarVendedor(@ModelAttribute @Validated Vendedor vendedor, Model model, RedirectAttributes flash){
        /*
        Vendedor vendedorGuardado = vendedorService.registrarVendedor(vendedor);
        model.addAttribute("vendedorId", vendedorGuardado.getId());
         */
        Vendedor vendedorGuardado = vendedorService.buscarVendedorRegistrado(vendedor);
        model.addAttribute("vendedorId", vendedorGuardado.getId());
        flash.addFlashAttribute("success", "Datos del vendedor guardados. Continúe con el vehículo.");
        return "redirect:/vehiculos/registrar";
    }

    @PostMapping("/vehiculo/oferta")
    public String estimarOferta(@ModelAttribute("vehiculo") @Validated Vehiculo vehiculo,
                                @RequestParam("imagenFile") MultipartFile image,
                                Model model,
                                HttpSession session,
                                RedirectAttributes flash) throws Exception {

        // 1. GUARDADO TEMPORAL DE LA IMAGEN
        // Solo guardar si es la primera vez que se sube la imagen (vehiculo.getUrlImagen() es nulo)
        if (vehiculo.getUrlImagen() == null || vehiculo.getUrlImagen().isEmpty()) {
            if (image.isEmpty()) {
                flash.addFlashAttribute("error", "Debe seleccionar una imagen.");
                flash.addFlashAttribute("vehiculo", vehiculo);
                return "redirect:/vehiculos/registrar";
            }
            String uniqueFileName = uploadFileService.copy(image);
            vehiculo.setUrlImagen(uniqueFileName); // Almacena la ruta temporal en el objeto
        }

        // 2. CÁLCULO DE LA OFERTA (NO se guarda en DB todavía)
        // Usa el servicio de vehículo con la lógica de estimación
        Double ofertaEstimada = vehiculoService.estimarPrecioCorregido(
                vehiculo.getPrecio(),
                vehiculo.getKilometraje(),
                vehiculo.getAnioFabricacion()
        );
        // 3. ALMACENAMIENTO TEMPORAL EN SESIÓN
        session.setAttribute(SESSION_OFERTA_KEY, ofertaEstimada);
        // 4. RETORNA LA VISTA
        model.addAttribute(SESSION_OFERTA_KEY, ofertaEstimada);
        return "vender-vehiculo";
    }

    //Mostar formulario de datos de bancarios
    @GetMapping("/vehiculo/datos-bancarios")
    public String mostrarFormularioBancario(Model model, @ModelAttribute("vendedorId") Integer vendedorId) {
        if (vendedorId == null) {
            return "redirect:/vendedor/registrar";
        }
        model.addAttribute("datosBancarios", new DatosBancarios());
        return "registrar-datos-bancarios"; // Crear esta nueva vista
    }

    //  Guardado Final de la Venta
    @PostMapping("/vehiculo/finalizar-registro")
    public String finalizarRegistroVenta(@ModelAttribute @Validated DatosBancarios datosBancarios,
                                         BindingResult result,
                                         @ModelAttribute("vendedorId") Integer vendedorId,
                                         @ModelAttribute("vehiculo") Vehiculo vehiculo,
                                         SessionStatus status,
                                         HttpSession session,
                                         RedirectAttributes flash) {
        try {
            if (vendedorId == null || vendedorId == 0) {
                flash.addFlashAttribute("error", "Error: Sesión de vendedor perdida.");
                return "redirect:/vendedor/registrar";
            }
            if (result.hasErrors()) {
                flash.addFlashAttribute("error", "Error en los datos bancarios. Revise el formulario.");
                return "redirect:/vehiculo/datos-bancarios";
            }

            // VALIDACIÓN DEL PRECIO ESTIMADO
            if (vehiculo.getPrecio() == 0.0) { // Usamos 0.0 si es double primitivo
                throw new Exception("Oferta no calculada o perdida de sesión.");
            }

            // 1. GUARDAR DATOS BANCARIOS y obtener el Vendedor
            Vendedor vendedor = vendedorService.guardarDatosBancarios(vendedorId, datosBancarios);

            // 2. VINCULAR VENDEDOR al VEHÍCULO
            vehiculo.setVendedor(vendedor);
            // 3. GUARDAR VEHÍCULO DEFINITIVAMENTE (Persistencia Final)
            Double ofertaAceptada = (Double) session.getAttribute(SESSION_OFERTA_KEY);
            vehiculo.setPrecio(ofertaAceptada);
            vehiculoService.agregarVehiculo(vehiculo);

            // 4. LIMPIAR LA SESIÓN
            status.setComplete();

            flash.addFlashAttribute("success", "¡Vehículo y datos de pago registrados con éxito!.");
            return "redirect:/vehiculos";

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al finalizar el registro: " + e.getMessage());
            return "redirect:/vehiculo/datos-bancarios";
        }
    }
    @GetMapping("/contrato/descargar/{id}")
    public ResponseEntity<Resource> descargarContrato(@PathVariable int id) {
        try {
            // 1. Obtener el Contrato (con Cliente y Vehiculo)
            Contrato contrato = contratoService.obtenerContratoPorId(id);

            if (contrato == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. Generar el PDF como un array de bytes usando el servicio
            byte[] pdfBytes = pdfGeneratorService.generarContratoPDF(contrato); // Llama al servicio

            // 3. Configurar la respuesta HTTP para la descarga
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            String fileName = "Contrato_Compraventa_" + contrato.getId() + ".pdf";

            return ResponseEntity.ok()
                    // Indica al navegador que descargue el archivo
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"")
                    // Define el tipo de archivo
                    .contentType(MediaType.APPLICATION_PDF)
                    // Establece la longitud del contenido
                    .contentLength(pdfBytes.length)
                    // Cuerpo de la respuesta
                    .body(resource);

        } catch (Exception e) {
            // Manejo de errores de generación de PDF o base de datos
            System.err.println("Error al descargar el contrato: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

   //Ver aviso de privacidad
    @GetMapping("/aviso-privacidad")
    public String avisoPrivacidad() {
        return "aviso-privacidad";
    }



}