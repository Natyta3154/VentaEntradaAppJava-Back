package com.example.venta_entrada.ventas.services;

import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.ventas.models.Entrada;
import com.example.venta_entrada.ventas.utils.QrCodeGeneratorUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void enviarEntradasPorEmail(Usuario usuario, List<Entrada> entradas) {
        if (entradas.isEmpty()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(usuario.getEmail());
            helper.setSubject("¡Tus entradas están listas! - " + entradas.get(0).getEvento().getNombre());

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<h1>Hola ").append(usuario.getNombre()).append(",</h1>");
            htmlContent.append("<p>¡Gracias por tu compra! Aquí están tus entradas para <b>")
                       .append(entradas.get(0).getEvento().getNombre())
                       .append("</b>.</p>");
            htmlContent.append("<p>Por favor presenta los códigos QR adjuntos en la puerta del evento.</p>");
            
            htmlContent.append("<br>");
            
            for (int i = 0; i < entradas.size(); i++) {
                htmlContent.append("<p>Entrada ").append(i + 1).append(": <b>")
                           .append(entradas.get(i).getTipoEntrada().getNombre())
                           .append("</b></p>");
            }

            helper.setText(htmlContent.toString(), true);

            // Adjuntar imágenes QR
            for (int i = 0; i < entradas.size(); i++) {
                Entrada entrada = entradas.get(i);
                byte[] qrBytes = QrCodeGeneratorUtil.generateQrCodeBytes(entrada.getCodigoQr().toString());
                helper.addAttachment("entrada-" + (i + 1) + ".png", new ByteArrayResource(qrBytes));
            }

            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", usuario.getEmail());

        } catch (MessagingException e) {
            log.error("Error al enviar el email con las entradas a: {}", usuario.getEmail(), e);
        }
    }
}
