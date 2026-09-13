package com.example.venta_entrada.contacto.services;

import com.example.venta_entrada.contacto.dtos.ContactoRequestDTO;
import com.example.venta_entrada.contacto.models.MensajeContacto;
import com.example.venta_entrada.contacto.repositories.ContactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public MensajeContacto guardarMensaje(ContactoRequestDTO dto) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(dto.getNombre());
        mensaje.setEmail(dto.getEmail());
        mensaje.setAsunto(dto.getAsunto());
        mensaje.setMensaje(dto.getMensaje());
        
        return contactoRepository.save(mensaje);
    }

    public List<MensajeContacto> obtenerTodos() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc();
    }

    public MensajeContacto marcarComoLeido(Long id) {
        Optional<MensajeContacto> optMensaje = contactoRepository.findById(id);
        if (optMensaje.isPresent()) {
            MensajeContacto mensaje = optMensaje.get();
            mensaje.setLeido(true);
            return contactoRepository.save(mensaje);
        }
        throw new RuntimeException("Mensaje no encontrado");
    }

    public MensajeContacto responderMensaje(Long id, String respuesta) {
        Optional<MensajeContacto> optMensaje = contactoRepository.findById(id);
        if (optMensaje.isPresent()) {
            MensajeContacto mensaje = optMensaje.get();
            
            // Enviar correo
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mailFrom);
            mailMessage.setTo(mensaje.getEmail());
            mailMessage.setSubject("Re: " + mensaje.getAsunto());
            mailMessage.setText(respuesta + "\n\n--- Tu mensaje original ---\n" + mensaje.getMensaje());
            
            mailSender.send(mailMessage);
            
            // Marcar como leído
            mensaje.setLeido(true);
            return contactoRepository.save(mensaje);
        }
        throw new RuntimeException("Mensaje no encontrado");
    }
}
