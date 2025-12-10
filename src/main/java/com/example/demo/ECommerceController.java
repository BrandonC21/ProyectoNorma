package com.example.demo;
import com.example.demo.persistencia.entidades.*;
import com.example.demo.persistencia.repositorio.ContratoRepo;
import com.example.demo.persistencia.repositorio.VehiculoRepo;
import com.example.demo.servicio.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
            // Filtra por marca Y que NO esté vendido
            model.addAttribute("vehiculos", vehiculoRepository.findByMarcaAndVendidoFalse(marca));
        } else {
            // model.addAttribute("vehiculos", vehiculoRepository.findAll());
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
        return "redirect:/acuerdo/" + vehiculoId + "/" + clienteGuardado.getId();
    }

    // 5. Vista de Acuerdo (Click-Wrap)
    @GetMapping("/acuerdo/{vehiculoId}/{clienteId}")
    public String mostrarAcuerdo(@PathVariable int vehiculoId, @PathVariable int clienteId, Model model) {
        model.addAttribute("cliente", clienteService.obtenerCliente(clienteId)); // Obtiene cliente DESCIFRADO temporalmente
        model.addAttribute("vehiculo", vehiculoRepository.findById(vehiculoId).orElseThrow());
        model.addAttribute("versionActual", ContratoService.VERSION_ACTUAL_ACUERDO);
        return "acuerdo-terminos";
    }

    // 6. Finalizar Contrato (POST) - Invoca Click-Wrap Service
    @PostMapping("/contrato/finalizar")
    public String finalizarContrato(@RequestParam int vehiculoId, @RequestParam int clienteId, @RequestParam String versionAceptada,
                                    RedirectAttributes flash) {
        try {
            Contrato contrato = contratoService.generarContrato(clienteId, vehiculoId, versionAceptada); // ¡VALIDACIÓN CLICK-WRAP!
            vehiculoService.marcarComoVendido(vehiculoId);
            flash.addFlashAttribute("success", "Contrato de venta finalizado y vehículo retirado del inventario.");
            return "redirect:/confirmacion/" + contrato.getId();
        }catch (Exception e){
            // Manejo de errores: si falla el guardado del contrato o la eliminación del vehículo.
            flash.addFlashAttribute("error", "Error al finalizar la venta y contrato: " + e.getMessage());
            return "redirect:/acuerdo/" + vehiculoId + "/" + clienteId;
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

    // 11 Guardar Vehiculo e imagen
    /*
    @PostMapping("/save")
    public String saveMeme(@Validated @ModelAttribute("vehiculo") Vehiculo vehiculo, BindingResult result, Model model,
                           @RequestParam("imagenFile") MultipartFile image, RedirectAttributes flash, SessionStatus status)
            throws Exception {
        if (result.hasErrors()) {
            System.out.println(result.getFieldError());
            return "vender-vehiculo";
        } else {
            if (!image.isEmpty()) {
                if (vehiculo.getId() > 0 && vehiculo.getUrlImagen() != null && vehiculo.getUrlImagen().length() > 0) {
                    uploadFileService.delete(vehiculo.getUrlImagen());
                }
                String uniqueFileName = uploadFileService.copy(image);
                vehiculo.setUrlImagen(uniqueFileName);
            }
            vehiculoService.agregarVehiculo(vehiculo);
            status.setComplete();
        }
        return "redirect:";
    }*/

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
        //vehiculo.setPrecio(ofertaEstimada);
        // El objeto 'vehiculo' (con la URL temporal) se mantiene por @SessionAttributes

        // 4. RETORNA LA VISTA
        model.addAttribute(SESSION_OFERTA_KEY, ofertaEstimada);
        return "vender-vehiculo";
    }
    /*
    @PostMapping("/vehiculo/save")
    public String finalizarVenta(@ModelAttribute("vehiculo") Vehiculo vehiculo, // Obtiene el objeto completo de la Sesión
                                 HttpSession session,
                                 SessionStatus status,
                                 RedirectAttributes flash) {

        // 1. RECUPERAR OFERTA
        Double ofertaAceptada = (Double) session.getAttribute(SESSION_OFERTA_KEY);

        if (ofertaAceptada == null || vehiculo.getUrlImagen() == null || vehiculo.getUrlImagen().isEmpty()) {
            flash.addFlashAttribute("error", "Error: La sesión expiró o faltan datos. Inicie el proceso de nuevo.");
            // Si hay error, intentamos borrar la imagen temporal subida en el paso 1
            if (vehiculo.getUrlImagen() != null && !vehiculo.getUrlImagen().isEmpty()) {
                try { uploadFileService.delete(vehiculo.getUrlImagen()); } catch (Exception ignored) {}
            }
            return "redirect:/admin/vehiculos/registrar";
        }

        // 2. GUARDADO DEFINITIVO EN BASE DE DATOS
        vehiculo.setPrecio(ofertaAceptada);

        // ESTE ES EL PASO CRÍTICO: Persistencia
        vehiculoService.agregarVehiculo(vehiculo);

        // 3. LIMPIEZA DE SESIÓN
        session.removeAttribute(SESSION_OFERTA_KEY);
        status.setComplete(); // Limpia el atributo 'vehiculo' de la sesión

        flash.addFlashAttribute("success", "¡Venta finalizada con éxito! Vehículo registrado.");
        return "redirect:/finalizar-registro"; // Redirigir al catálogo
    } */
    @GetMapping("/vehiculo/datos-bancarios")
    public String mostrarFormularioBancario(Model model, @ModelAttribute("vendedorId") Integer vendedorId) {
        if (vendedorId == null) {
            return "redirect:/vendedor/registrar";
        }
        model.addAttribute("datosBancarios", new DatosBancarios());
        return "registrar-datos-bancarios"; // Crear esta nueva vista
    }

    // 14. Guardado Final de la Venta (Paso 3.2: Guarda Banco, Vincula Vendedor, Guarda Vehículo)
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


}