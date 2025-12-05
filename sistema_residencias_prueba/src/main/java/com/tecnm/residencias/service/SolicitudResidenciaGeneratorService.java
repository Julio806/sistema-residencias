package com.tecnm.residencias.service;

import com.tecnm.residencias.entity.Alumno;
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class SolicitudResidenciaGeneratorService {

    public Path generarOficioSolicitudResidencia(List<Alumno> alumnos, InputStream plantillaStream) throws IOException {
        // Cargar la plantilla completa (con logos, membretes, encabezados y pies de página)
        try (XWPFDocument doc = new XWPFDocument(plantillaStream)) {

            // === FECHA ===
            LocalDate fechaActual = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

            XWPFParagraph fechaParrafo = doc.createParagraph();
            fechaParrafo.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun fechaRun = fechaParrafo.createRun();
            fechaRun.setText("Frontera Comalapa, Chiapas, " + fechaActual.format(formatter));
            fechaRun.setFontSize(11);

            // === NÚMERO DE OFICIO ===
            XWPFParagraph oficio = doc.createParagraph();
            oficio.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun oficioRun = oficio.createRun();
            oficioRun.setText("OFICIO No. DEP/012/2025");
            oficioRun.setBold(true);
            oficioRun.setFontSize(11);

            // === DESTINATARIO ===
            XWPFParagraph destinatario = doc.createParagraph();
            destinatario.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun destRun = destinatario.createRun();
            destRun.setText("ING. DILVAR HERNANDES AYALA");
            destRun.addBreak();
            destRun.setText("JEFE DEL DEPARTAMENTO DE INGENIERIAS");
            destRun.addBreak();
            destRun.setText("P R E S E N T E");
            destRun.setBold(true);
            destRun.setFontSize(11);

            // === CUERPO DEL TEXTO ===
            XWPFParagraph cuerpo = doc.createParagraph();
            cuerpo.setAlignment(ParagraphAlignment.BOTH);
            XWPFRun cuerpoRun = cuerpo.createRun();
            cuerpoRun.setFontSize(11);
            cuerpoRun.addBreak();
            cuerpoRun.setText("Sirva el presente para notificar la recepción de " + alumnos.size() +
                    " solicitudes de estudiantes de la carrera de " + alumnos.get(0).getCarrera().toUpperCase() +
                    " para la realización de sus Residencias Profesionales, y a la par solicito la revisión y dictamen de los proyectos que se anexan a continuación:");
            cuerpoRun.addBreak();
            cuerpoRun.addBreak();

            // === TABLA ===
            XWPFTable table = doc.createTable();
            table.setWidth("100%");

            // Encabezado
            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("Nombre");
            header.addNewTableCell().setText("Apellidos");
            header.addNewTableCell().setText("NoCtrl");
            header.addNewTableCell().setText("Proyecto");

            // Estilo encabezado
            for (XWPFTableCell cell : header.getTableCells()) {
                XWPFParagraph p = cell.getParagraphs().get(0);
                p.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setFontSize(11);
            }

            // Filas con alumnos
            for (Alumno a : alumnos) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(a.getNombres());
                row.getCell(1).setText(a.getApellidos());
                row.getCell(2).setText(a.getNumeroControl());
                row.getCell(3).setText(a.getNombreProyecto() != null ? a.getNombreProyecto() : "(sin proyecto)");
            }

            // Bordes de tabla
            table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");

            // === CIERRE INSTITUCIONAL ===
            XWPFParagraph cierre = doc.createParagraph();
            cierre.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun cierreRun = cierre.createRun();
            cierreRun.addBreak();
            cierreRun.addBreak();
            cierreRun.setText("A T E N T A M E N T E");
            cierreRun.addBreak();
            cierreRun.setText("Excelencia en Educación Tecnológica®");
            cierreRun.addBreak();
            cierreRun.addBreak();
            cierreRun.setText("LINO JEREMIAS RAMIREZ PÉREZ");
            cierreRun.addBreak();
            cierreRun.setText("JEFE DE LA DIVISIÓN DE ESTUDIOS PROFESIONALES");

            // === GUARDAR DOCUMENTO ===
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "Oficio_SolicitudResidencia_" + timestamp + ".docx";
            Path salida = Paths.get("documentos_generados", nombreArchivo);
            Files.createDirectories(salida.getParent());

            try (OutputStream out = Files.newOutputStream(salida)) {
                doc.write(out);
            }

            return salida;
        }
    }

}
