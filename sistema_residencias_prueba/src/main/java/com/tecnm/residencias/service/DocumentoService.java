package com.tecnm.residencias.service;

import com.tecnm.residencias.dto.DocumentoResumen;
import com.tecnm.residencias.entity.Documento;
import com.tecnm.residencias.repository.DocumentoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.Conversion;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentoService {
    private final DocumentoRepository documentoRepo;

    public DocumentoService(DocumentoRepository documentoRepo) {
        this.documentoRepo = documentoRepo;
    }
    @Autowired
    private DocumentoRepository documentoRepository;

    // Guardar un nuevo documento
    public Documento guardar(Documento doc) {
        return documentoRepository.save(doc);
    }

    // Obtener metadatos de documentos por alumno (sin datos LOB)
    public List<DocumentoResumen> obtenerResumenesPorAlumno(Long alumnoId) {
        return documentoRepository.findResumenesByAlumnoId(alumnoId);
    }

    // Obtener documento completo por ID
    @Transactional(readOnly = true)
    public Optional<Documento> obtenerPorId(Long id) {
        return documentoRepository.findById(id);
    }

    // Eliminar documento
    public void eliminar(Long id) {
        documentoRepository.deleteById(id);
    }

    // Aceptar un documento
    public void aceptar(Long id) {
        documentoRepository.findById(id).ifPresent(doc -> {
            doc.setAceptado(true);
            documentoRepository.save(doc);
        });
    }

    // Rechazar un documento
    public void rechazar(Long id) {
        documentoRepository.findById(id).ifPresent(doc -> {
            doc.setAceptado(false);
            documentoRepository.save(doc);
        });
    }

    // Reemplazar el archivo del documento
    public void actualizarContenido(Long id, byte[] nuevoArchivo) {
        documentoRepository.findById(id).ifPresent(doc -> {
            doc.setDatos(nuevoArchivo);
            doc.setAceptado(null); // Se reinicia la revisión
            documentoRepository.save(doc);
        });
    }
    public List<DocumentoResumen> obtenerResumenesPorPeriodo(Long periodoId) {
        return documentoRepository.findResumenesByPeriodoId(periodoId);
    }

}
