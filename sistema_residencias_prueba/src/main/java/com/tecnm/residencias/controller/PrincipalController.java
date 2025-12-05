package com.tecnm.residencias.controller;

import com.tecnm.residencias.dto.CarreraCantidadDTO;
import com.tecnm.residencias.dto.DocumentoGeneradoResumen;
import com.tecnm.residencias.dto.DocumentoResumen;
import com.tecnm.residencias.entity.*;
import com.tecnm.residencias.repository.*;
import com.tecnm.residencias.service.DocumentoService;
import com.tecnm.residencias.service.NotificacionService;
import com.tecnm.residencias.service.PeriodoService;
import com.tecnm.residencias.service.PlantillaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Controller
public class PrincipalController {

    @Autowired private AlumnoRepository alumnoRepo;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private PeriodoRepository periodoRepository;
    @Autowired private PeriodoRepository periodoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DocumentoService documentoService;
    @Autowired private DocumentoRepository documentoRepo;
    @Autowired private NotificacionService notificacionService;
    @Autowired private PlantillaService plantillaService;
    @Autowired private PeriodoService periodoService;
    @Autowired private DocumentoGeneradoRepository documentoGeneradoRepo;


    @GetMapping("/")
    public String mostrarSelectorPeriodos(Model model) {
        List<PeriodoAcademico> periodos = periodoRepository.findAllByActivoTrue();
        model.addAttribute("periodos", periodos);
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        model.addAttribute("alumno", new Alumno());
        model.addAttribute("periodos", periodoRepo.findAll());
        return "registrarAlumno";
    }

    @PostMapping("/guardarAlumno")
    public String guardarAlumno(@ModelAttribute Alumno alumno,
                                @RequestParam String correo,
                                @RequestParam String clave,
                                RedirectAttributes redirectAttributes) {

        if (!correo.endsWith("@fcomalapa.tecnm.mx")) {
            redirectAttributes.addFlashAttribute("error", "El correo debe ser institucional (@fcomalapa.tecnm.mx)");
            return "redirect:/panel";
        }

        if (usuarioRepo.existsByCorreo(correo)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con ese correo.");
            return "redirect:/panel";
        }

        Rol rolAlumno = rolRepo.findByNombre("ALUMNO").orElse(null);
        if (rolAlumno == null) {
            redirectAttributes.addFlashAttribute("error", "El rol ALUMNO no está configurado.");
            return "redirect:/panel";
        }

        // Crear usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setClave(passwordEncoder.encode(clave));
        nuevoUsuario.setRol(rolAlumno);
        nuevoUsuario.setActivo(true);
        usuarioRepo.save(nuevoUsuario);

        // Asociar con alumno
        alumno.setUsuario(nuevoUsuario);
        alumnoRepo.save(alumno);

        // ✅ Mensaje para el admin
        redirectAttributes.addFlashAttribute("mensaje",
                "Alumno registrado correctamente. Usuario: " + correo + " | Contraseña: " + clave);

        return "redirect:/panel";
    }

    // =========================
    // 1. SELECCIONAR PERIODO
    // =========================
    @GetMapping("/seleccionar-periodo")
    public String seleccionarPeriodo(@RequestParam Long id,
                                     @RequestParam(value = "redirigir", required = false) String redirigir,
                                     HttpSession session,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttrs) {

        PeriodoAcademico periodo = periodoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado"));

        // ✅ Validar acceso
        boolean esAdmin = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!periodo.isActivo() && !esAdmin) {
            redirectAttrs.addFlashAttribute("error", "Acceso restringido. Solo administradores pueden ingresar a periodos anteriores.");
            return "redirect:/";
        }

        session.setAttribute("periodoActivo", id);

        if ("panel".equalsIgnoreCase(redirigir)) {
            return "redirect:/panel";
        } else {
            return "redirect:/login";
        }
    }


    // =========================
    // 2. PANEL DE CONTROL
    // =========================
    @GetMapping("/panel")
    public String panel(@RequestParam(value = "buscar", required = false) String buscar,
                        HttpSession session,
                        Model model) {

        Object rawPeriodo = session.getAttribute("periodoActivo");
        Long periodoId = null;

        if (rawPeriodo instanceof Long) {
            periodoId = (Long) rawPeriodo;
        } else {
            return "redirect:/seleccionar-periodo?error=falta_periodo";
        }

        // Obtener alumnos filtrados por periodo y búsqueda
        List<Alumno> alumnos;
        if (buscar != null && !buscar.isEmpty()) {
            alumnos = alumnoRepo.findByFiltroYPeriodo(buscar.toLowerCase(), periodoId);
        } else {
            alumnos = alumnoRepo.findByPeriodoId(periodoId);
        }

        // Ordenar alfabéticamente por carrera, luego por apellidos y nombres
        alumnos.sort(Comparator
                .comparing(Alumno::getCarrera, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Alumno::getApellidos, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Alumno::getNombres, String.CASE_INSENSITIVE_ORDER)
        );

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("buscar", buscar);
        model.addAttribute("alumno", new Alumno());
        model.addAttribute("periodos", periodoRepo.findAll());

        // Distribución por carrera (para gráfica)
        List<CarreraCantidadDTO> distribucion = alumnoRepo.contarPorCarreraYPeriodo(periodoId);
        if (distribucion == null) distribucion = new ArrayList<>();

        List<String> labels = distribucion.stream().map(CarreraCantidadDTO::getCarrera).toList();
        List<Long> valores = distribucion.stream().map(CarreraCantidadDTO::getCantidad).toList();

        model.addAttribute("labelsCarrera", labels);
        model.addAttribute("valoresCarrera", valores);

        return "panel";
    }


    // =========================
    // 3. CREAR PERIODO
    // =========================
    @PostMapping("/crear-periodo")
    public String guardarPeriodo(@ModelAttribute PeriodoAcademico periodo, RedirectAttributes redirectAttrs) {
        periodo.setId(null); // 👈 Siempre fuerza a que sea un nuevo registro
        periodoRepo.save(periodo);
        redirectAttrs.addFlashAttribute("mensaje", "Periodo creado correctamente.");
        return "redirect:/panel"; // O donde quieras redirigir después
    }

    @GetMapping("/nuevo-periodo")
    public String mostrarFormularioNuevoPeriodo(Model model) {
        model.addAttribute("periodo", new PeriodoAcademico()); // 👈 Objeto vacío
        return "nuevo-periodo"; // Nombre del HTML Thymeleaf
    }

    // =========================
    // 4. CAMBIAR ESTADO (ACTIVO/INACTIVO)
    // =========================
    @PostMapping("/cambiarEstado")
    public String cambiarEstado(@RequestParam Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes redirectAttrs) {

        PeriodoAcademico periodo = periodoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado"));

        periodo.setActivo(activo);
        periodoRepo.save(periodo);

        redirectAttrs.addFlashAttribute("mensaje", "Estado del periodo actualizado correctamente");
        return "redirect:/panel";
    }

    @GetMapping("/mi_perfil")
    public String mostrarPerfil(Model model, Principal principal) {
        String correo = principal.getName();
        Usuario usuario = usuarioRepo.findByCorreo(correo).orElse(null);
        if (usuario == null) return "redirect:/login?error";

        Alumno alumno = alumnoRepo.findByUsuario(usuario);
        List<DocumentoResumen> documentos = documentoService.obtenerResumenesPorAlumno(alumno.getId());

        model.addAttribute("alumno", alumno);
        model.addAttribute("documentos", documentos);

        List<Notificacion> notificaciones = notificacionService.obtenerNoLeidas(alumno);
        model.addAttribute("notificaciones", notificaciones);
        notificacionService.marcarTodasComoLeidas(alumno);  // opcional: marca como leídas después de mostrar

        return "mi_perfil";
    }


    @PostMapping("/mi_perfil/guardar")
    public String guardarPerfilAlumno(@ModelAttribute Alumno nuevoAlumno, Authentication auth) {
        String correo = auth.getName();
        Usuario usuario = usuarioRepo.findByCorreo(correo).orElse(null);

        if (usuario != null) {
            nuevoAlumno.setUsuario(usuario);
            alumnoRepo.save(nuevoAlumno);
        }
        return "redirect:/mi_perfil";
    }

    @GetMapping("/alumnos/{id}/documentos")
    public String verDocumentosPorAlumno(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoRepo.findById(id).orElse(null);
        if (alumno == null) return "redirect:/panel";

        List<DocumentoResumen> documentos = documentoService.obtenerResumenesPorAlumno(id);
        model.addAttribute("alumno", alumno);
        model.addAttribute("documentos", documentos);
        return "documentos_admin";
    }


    @GetMapping("/admin/documentos")
    public String listarDocumentosPorPeriodo(Model model, HttpSession session) {
        Long periodoId = (Long) session.getAttribute("periodoActivo");
        if (periodoId == null) return "redirect:/";

        List<Documento> documentos = documentoRepo.findByPeriodoId(periodoId);
        model.addAttribute("documentos", documentos);
        return "documentos_admin";
    }
    @PostMapping("/alumnos/editar")
    public String editarAlumno(@ModelAttribute Alumno alumnoForm) {
        Alumno alumnoExistente = alumnoRepo.findById(alumnoForm.getId()).orElse(null);

        if (alumnoExistente != null) {
            alumnoExistente.setNombres(alumnoForm.getNombres());
            alumnoExistente.setApellidos(alumnoForm.getApellidos());
            alumnoExistente.setNumeroControl(alumnoForm.getNumeroControl());
            alumnoExistente.setCarrera(alumnoForm.getCarrera());
            alumnoExistente.setNombreProyecto(alumnoForm.getNombreProyecto());
            alumnoRepo.save(alumnoExistente);
        }

        return "redirect:/panel";
    }

    @GetMapping("/documentos")
    public String vistaGeneracionDocumentos(Model model, HttpServletRequest request) {
        Long periodoId = (Long) request.getSession().getAttribute("periodoActivo");

        if (periodoId == null) {
            return "redirect:/seleccionar-periodo?error=periodo-requerido";
        }

        // ✅ Documentos generados (vía DTO para evitar LOB)
        List<DocumentoGeneradoResumen> generados = documentoGeneradoRepo.findResumenByPeriodo(periodoId);
        model.addAttribute("documentosGenerados", generados);

        // ✅ Carreras únicas de alumnos dentro del periodo activo
        List<String> carreras = alumnoRepo.findAll().stream()
                .filter(a -> a.getPeriodo() != null && a.getPeriodo().getId().equals(periodoId))
                .map(a -> a.getCarrera().toUpperCase())
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("carreras", carreras);

        // ✅ Plantillas (usando solo los metadatos, si lo deseas)
        model.addAttribute("plantillas", plantillaService.listarResumenes());

        return "documento";
    }

    @GetMapping("/ingenierias")
    public String vistaIngenieria(Model model, HttpServletRequest request) {
        Long periodoId = (Long) request.getSession().getAttribute("periodoActivo");
        if (periodoId == null) return "redirect:/seleccionar-periodo?error=periodo-requerido";

        List<DocumentoGeneradoResumen> documentos = documentoGeneradoRepo.findResumenByPeriodo(periodoId); // asegúrate que contenga 'aceptado'
        model.addAttribute("documentos", documentos);

        return "panel_ingenierias";
    }

    @PostMapping("/rechazar-ingenieria/{id}")
    public String rechazarDesdeIngenieria(@PathVariable Long id, @RequestParam("motivo") String motivo) {
        Documento doc = documentoRepo.findById(id).orElse(null);
        if (doc != null) {
            doc.setAceptado(false);
            documentoRepo.save(doc);

            notificacionService.crear(doc.getAlumno(), "Tu documento \"" + doc.getNombre() + "\" fue RECHAZADO por Ingeniería. Motivo: " + motivo);
        }
        return "redirect:/panel_ingenierias";
    }
    @PostMapping("/alumnos/eliminar/{id}")
    public String eliminarAlumno(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (alumnoRepo.existsById(id)) {
            alumnoRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Alumno eliminado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "El alumno no existe o ya fue eliminado.");
        }
        return "redirect:/panel";
    }



}


