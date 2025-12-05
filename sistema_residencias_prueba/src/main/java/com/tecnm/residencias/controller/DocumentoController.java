package com.tecnm.residencias.controller;

import com.tecnm.residencias.dto.DocumentoGeneradoResumen;
import com.tecnm.residencias.entity.*;
import com.tecnm.residencias.repository.*;
import com.tecnm.residencias.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

@Controller
@RequestMapping("/documentos")
public class DocumentoController {
    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }
    @Autowired
    private DocumentoRepository documentoRepo;
    @Autowired
    private AlumnoRepository alumnoRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private NotificacionService notificacionService;
    @Autowired
    private SolicitudResidenciaGeneratorService documentoGeneradorService;
    @Autowired
    private PlantillaService plantillaService;
    @Autowired
    private PlantillaRepository plantillaRepo;
    @Autowired
    private PeriodoService periodoService;
    @Autowired
    private DocumentoGeneradoRepository documentoGeneradoRepo;
    @Autowired
    private PeriodoRepository periodoRepo;
    @Autowired
    private SolicitudResidenciaGeneratorService generador;

    // SUBIR MÚLTIPLES DOCUMENTOS
    @PostMapping("/subir")
    public String subir(@RequestParam("archivo") List<MultipartFile> archivos, Authentication auth) throws IOException {
        Usuario usuario = usuarioRepo.findByCorreo(auth.getName()).orElse(null);
        if (usuario == null || archivos.isEmpty()) return "redirect:/mi_perfil";

        Alumno alumno = alumnoRepo.findByUsuario(usuario);

        for (MultipartFile archivo : archivos) {
            if (!archivo.isEmpty()) {
                Documento doc = new Documento();
                doc.setNombre(archivo.getOriginalFilename());
                doc.setDatos(archivo.getBytes());  //  AQUÍ SE GUARDA EL CONTENIDO BINARIO
                doc.setAceptado(null);
                doc.setAlumno(alumno);
                doc.setPeriodo(alumno.getPeriodo());
                documentoRepo.save(doc);
            }
        }

        return "redirect:/mi_perfil";
    }


    // DESCARGAR DOCUMENTO
    @Transactional(readOnly = true)
    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        Documento doc = documentoRepo.findById(id).orElse(null);
        if (doc == null || doc.getDatos() == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNombre() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(doc.getDatos()));
    }

    // ELIMINAR DOCUMENTO
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        documentoRepo.findById(id).ifPresent(documentoRepo::delete);
        return "redirect:/mi_perfil";
    }

    // EDITAR (REEMPLAZAR) DOCUMENTO
    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) throws IOException {
        Documento doc = documentoRepo.findById(id).orElse(null);
        if (doc != null && !archivo.isEmpty()) {
            doc.setNombre(archivo.getOriginalFilename());
            doc.setDatos(archivo.getBytes());
            doc.setAceptado(null);
            documentoRepo.save(doc);
        }
        return "redirect:/mi_perfil";
    }

    // ACEPTAR DOCUMENTO (CON NOTIFICACIÓN)
    @PostMapping("/aceptar/{id}")
    public String aceptar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Documento doc = documentoRepo.findById(id).orElse(null);

        if (doc == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: documento no encontrado.");
            return "redirect:/panel"; // Puedes cambiar a donde sea más apropiado
        }

        doc.setAceptado(true);
        documentoRepo.save(doc);

        String mensaje = String.format("Tu documento \"%s\" ha sido ACEPTADO.", doc.getNombre());
        notificacionService.crear(doc.getAlumno(), mensaje);

        redirectAttributes.addFlashAttribute("mensaje", "Documento aceptado correctamente.");
        return "redirect:/alumnos/" + doc.getAlumno().getId() + "/documentos";
    }


    @PostMapping("/rechazar/{id}")
    public String rechazarDocumento(
            @PathVariable Long id,
            @RequestParam("motivo") String motivo,
            @RequestParam("fechaLimite") String fechaLimite,
            RedirectAttributes redirectAttributes
    ) {
        Documento doc = documentoRepo.findById(id).orElse(null);
        if (doc != null) {
            doc.setAceptado(false);
            documentoRepo.save(doc);

            String mensaje = String.format("Tu documento \"%s\" fue RECHAZADO.\nMotivo: %s.\nPuedes volver a subirlo antes del %s.",
                    doc.getNombre(),
                    motivo != null && !motivo.isBlank() ? motivo : "Sin especificar",
                    fechaLimite != null && !fechaLimite.isBlank() ? fechaLimite : "Sin fecha límite"
            );

            notificacionService.crear(doc.getAlumno(), mensaje);
            redirectAttributes.addFlashAttribute("mensaje", "Documento rechazado con observación.");
        }
        return "redirect:/alumnos/" + (doc != null ? doc.getAlumno().getId() : 0) + "/documentos";
    }


    @GetMapping("/todos")
    public String verTodosLosDocumentos(HttpSession session, Model model) {
        Long periodoId = (Long) session.getAttribute("periodoActivo");
        if (periodoId == null) {
            return "redirect:/?error=periodo";
        }

        List<Documento> documentos = documentoRepo.findByPeriodoId(periodoId);
        model.addAttribute("documentos", documentos);
        return "admin_documentos";
    }

    @GetMapping("/documentos-admin")
    public String verDocumentosAdmin(HttpSession session, Model model) {
        Long periodoId = (Long) session.getAttribute("periodoActivo");
        if (periodoId == null) {
            return "redirect:/?error=periodo";
        }

        List<Documento> documentos = documentoRepo.findByPeriodoId(periodoId); //  SÓLO DEL PERIODO ACTIVO
        model.addAttribute("documentos", documentos);
        return "documentos_admin"; //  Esta es la vista que ya tienes
    }

    @PostMapping("/generar")
    public String generarDocumento(@RequestParam String tipo,
                                   @RequestParam String carrera,
                                   HttpServletRequest request,
                                   RedirectAttributes redirect) {
        try {
            Long periodoId = (Long) request.getSession().getAttribute("periodoActivo");
            if (periodoId == null) {
                redirect.addFlashAttribute("error", "Periodo no seleccionado");
                return "redirect:/documentos";
            }

            // 🔎 Obtener alumnos de la carrera
            List<Alumno> alumnos = alumnoRepo.findByCarreraIgnoreCaseAndPeriodoId(carrera, periodoId);
            if (alumnos.isEmpty()) {
                redirect.addFlashAttribute("error", "No hay alumnos registrados en la carrera: " + carrera);
                return "redirect:/documentos";
            }

            // 🔎 Obtener plantilla desde la base de datos (incluyendo archivo .docx)
            Optional<Plantilla> plantillaOpt = plantillaService.obtenerPorTipo(tipo);
            if (plantillaOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Plantilla no encontrada para el tipo: " + tipo);
                return "redirect:/documentos";
            }

            byte[] archivo = plantillaOpt.get().getArchivo();
            if (archivo == null || archivo.length == 0) {
                redirect.addFlashAttribute("error", "La plantilla está vacía o corrupta");
                return "redirect:/documentos";
            }

            // 🛠️ Generar documento usando Apache POI
            ByteArrayInputStream plantillaStream = new ByteArrayInputStream(archivo);
            Path pathGenerado = generador.generarOficioSolicitudResidencia(alumnos, plantillaStream);

            // 💾 Guardar documento generado
            DocumentoGenerado doc = new DocumentoGenerado();
            doc.setTipo(tipo);
            doc.setCarrera(carrera);
            doc.setNombreArchivo(pathGenerado.getFileName().toString());
            doc.setDatos(Files.readAllBytes(pathGenerado));
            doc.setFecha(LocalDateTime.now());
            doc.setPeriodo(new PeriodoAcademico(periodoId));
            doc.setAceptado(null);
            documentoGeneradoRepo.save(doc);

            redirect.addFlashAttribute("mensaje", "Documento generado exitosamente");

        } catch (Exception e) {
            e.printStackTrace(); // para debug en consola
            redirect.addFlashAttribute("error", "Error al generar el documento: " + e.getMessage());
        }

        return "redirect:/documentos";
    }

    @PostMapping("/plantillas/subir")
    public String subirPlantilla(@RequestParam("tipo") String tipo,
                                 @RequestParam("archivo") MultipartFile archivo,
                                 RedirectAttributes redirect) {
        try {
            plantillaService.guardar(tipo, archivo);
            redirect.addFlashAttribute("mensaje", "Plantilla subida exitosamente");
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "Error al subir plantilla: " + e.getMessage());
        }
        return "redirect:/documentos";
    }

    @GetMapping("/plantillas/descargar/{id}")
    public ResponseEntity<Resource> descargarPlantilla(@PathVariable Long id) {
        return plantillaService.descargar(id);
    }

    @PostMapping("/plantillas/eliminar/{id}")
    public String eliminarPlantilla(@PathVariable Long id, RedirectAttributes redirect) {
        plantillaService.eliminar(id);
        redirect.addFlashAttribute("mensaje", "Plantilla eliminada correctamente");
        return "redirect:/documentos";
    }

    @PostMapping("/plantillas/editar/{id}")
    public String editarPlantilla(@PathVariable Long id,
                                  @RequestParam("archivo") MultipartFile archivo,
                                  RedirectAttributes redirect) {
        try {
            plantillaService.actualizarContenido(id, archivo);
            redirect.addFlashAttribute("mensaje", "Plantilla actualizada correctamente");
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "Error al actualizar plantilla: " + e.getMessage());
        }
        return "redirect:/documentos";
    }
    @Transactional(readOnly = true)
    @GetMapping("/descargar-generado/{id}")
    public ResponseEntity<Resource> descargarDocumentoGenerado(@PathVariable Long id) {
        DocumentoGenerado doc = documentoGeneradoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento generado no encontrado"));

        ByteArrayResource recurso = new ByteArrayResource(doc.getDatos());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(doc.getDatos().length)
                .body(recurso);
    }
    @PostMapping("/documentos/firma/{id}")
    public String firmarDocumento(
            @PathVariable Long id,
            @RequestParam("firmaBase64") String firmaBase64,
            RedirectAttributes redirect) {
        try {
            DocumentoGenerado doc = documentoGeneradoRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

            if (firmaBase64 == null || !firmaBase64.startsWith("data:image")) {
                throw new IllegalArgumentException("Firma no válida");
            }

            // Convertir base64 a byte[]
            String base64Data = firmaBase64.split(",")[1];
            byte[] firmaBytes = Base64.getDecoder().decode(base64Data);

            // Abrir el documento original desde la base de datos
            try (InputStream inputStream = new ByteArrayInputStream(doc.getDatos());
                 XWPFDocument documento = new XWPFDocument(inputStream)) {

                // Insertar la firma al final del documento
                XWPFParagraph parrafoFirma = documento.createParagraph();
                parrafoFirma.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun runFirma = parrafoFirma.createRun();
                runFirma.setText("Firma del responsable:");
                runFirma.addBreak();

                int pictureType = Document.PICTURE_TYPE_PNG;
                String filename = "firma.png";

                runFirma.addPicture(new ByteArrayInputStream(firmaBytes),
                        pictureType, filename, Units.toEMU(150), Units.toEMU(80)); // tamaño: 150x80 px aprox

                // Guardar el documento con la firma incrustada
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                documento.write(outStream);
                byte[] documentoConFirma = outStream.toByteArray();

                // Actualizar el documento
                doc.setDatos(documentoConFirma);
                doc.setAceptado(true);
                documentoGeneradoRepo.save(doc);
            }

            redirect.addFlashAttribute("mensaje", "Documento firmado correctamente e incrustado");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al firmar el documento: " + e.getMessage());
        }
        return "redirect:/ingenierias";
    }



    @GetMapping("/ver/{id}")
    @Transactional(readOnly = true)
    public void verDocumento(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Documento doc = documentoRepo.findById(id).orElse(null); if (doc == null || doc.getDatos() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); return;
        }
        String tipo = "application/octet-stream";
        String nombre = doc.getNombre().toLowerCase(); if (nombre.endsWith(".pdf")) { tipo = "application/pdf"; }
        else if (nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".odt")) { tipo = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        response.setContentType(tipo); response.setHeader("Content-Disposition", "inline; filename=\"" + doc.getNombre() + "\""); response.getOutputStream().write(doc.getDatos()); }

    @GetMapping("/ingenierias/descargar/{id}")
    public ResponseEntity<Resource> descargarDesdeIngenieria(@PathVariable Long id) {
        DocumentoGenerado doc = documentoGeneradoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        ByteArrayResource recurso = new ByteArrayResource(doc.getDatos());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(doc.getDatos().length)
                .body(recurso);
    }

    @PostMapping("/ingenierias/rechazar/{id}")
    public String rechazarDocumentoDesdeIngenieria(
            @PathVariable Long id,
            @RequestParam("motivo") String motivo,
            @RequestParam("fechaLimite") String fechaLimite,
            RedirectAttributes redirect
    ) {
        DocumentoGenerado doc = documentoGeneradoRepo.findById(id).orElse(null);

        if (doc != null) {
            doc.setAceptado(false);
            documentoGeneradoRepo.save(doc);

            String mensaje = String.format("Documento \"%s\" fue RECHAZADO.\nMotivo: %s.\nCorregir antes del %s.",
                    doc.getNombreArchivo(),
                    motivo != null && !motivo.isBlank() ? motivo : "Sin motivo",
                    fechaLimite != null && !fechaLimite.isBlank() ? fechaLimite : "sin fecha límite"
            );

            // Si luego deseas agregar notificaciones persistentes a Ingeniería, aquí podrías hacerlo.
            redirect.addFlashAttribute("mensaje", mensaje);
        } else {
            redirect.addFlashAttribute("error", "Documento no encontrado.");
        }

        return "redirect:/ingenierias";
    }



}

