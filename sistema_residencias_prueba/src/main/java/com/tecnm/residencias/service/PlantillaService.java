package com.tecnm.residencias.service;

import com.tecnm.residencias.entity.Plantilla;
import com.tecnm.residencias.repository.PlantillaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.tecnm.residencias.dto.PlantillaResumen;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlantillaService {

    @Autowired
    private PlantillaRepository repo;

    public List<PlantillaResumen> listarResumenes() {
        return repo.findResumenes(); // ✅ Este es el nombre correcto definido en el repositorio
    }


    @Transactional(readOnly = true)
    public Optional<Plantilla> obtenerPorTipo(String tipo) {
        return repo.findByTipoIgnoreCase(tipo);
    }

    public void guardar(String tipo, MultipartFile archivo) throws IOException {
        Plantilla p = new Plantilla();
        p.setTipo(tipo);
        p.setNombreArchivo(archivo.getOriginalFilename());
        p.setArchivo(archivo.getBytes());
        p.setFechaSubida(LocalDateTime.now());
        repo.save(p);
    }

    public void actualizarContenido(Long id, MultipartFile archivo) throws IOException {
        Plantilla p = repo.findById(id).orElseThrow();
        p.setArchivo(archivo.getBytes());
        p.setNombreArchivo(archivo.getOriginalFilename());
        p.setFechaSubida(LocalDateTime.now());
        repo.save(p);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public ResponseEntity<Resource> descargar(Long id) {
        Plantilla p = repo.findById(id).orElseThrow();
        ByteArrayResource resource = new ByteArrayResource(p.getArchivo());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getNombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(p.getArchivo().length)
                .body(resource);
    }
}

// 🔹 Agregado para acceso directo si se necesita en el controlador
