package com.tecnm.residencias.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía un correo simple a un destinatario
     */
    public void enviarCorreo(String destino, String asunto, String mensaje) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(mensaje, false); // false = texto plano
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            // En producción podrías loguear o manejar la excepción de otra manera
        }
    }

    /**
     * Envía el mismo correo a múltiples destinatarios
     */
    public void enviarCorreo(String[] destinos, String asunto, String mensaje) {
        for (String correo : destinos) {
            enviarCorreo(correo, asunto, mensaje);
        }
    }
}
