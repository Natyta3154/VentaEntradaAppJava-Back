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

/**
 * Servicio de gestión de mensajes de contacto y soporte al cliente.
 * 
 * Funcionalidades:
 * 1. Guardar mensajes entrantes enviados desde el formulario público de contacto.
 * 2. Listar todos los mensajes ordenados por fecha descendente para los administradores.
 * 3. Marcar mensajes como leídos.
 * 4. Responder mensajes por correo electrónico directamente desde el panel de control.
 */
@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /**
     * Registra un nuevo mensaje enviado por un visitante o cliente.
     * 
     * @param dto Datos del formulario (nombre, email, asunto, mensaje).
     * @return Entidad {@link MensajeContacto} persistida.
     */
    public MensajeContacto guardarMensaje(ContactoRequestDTO dto) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(dto.getNombre());
        mensaje.setEmail(dto.getEmail());
        mensaje.setAsunto(dto.getAsunto());
        mensaje.setMensaje(dto.getMensaje());
        
        return contactoRepository.save(mensaje);
    }

    /**
     * Recupera todos los mensajes de contacto ordenados del más reciente al más antiguo.
     * 
     * @return Lista de mensajes recibidos.
     */
    public List<MensajeContacto> obtenerTodos() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc();
    }

    /**
     * Marca un mensaje como leído por parte del administrador.
     * 
     * @param id Identificador del mensaje.
     * @return {@link MensajeContacto} actualizado con leido=true.
     * @throws RuntimeException si el mensaje no existe.
     */
    public MensajeContacto marcarComoLeido(Long id) {
        Optional<MensajeContacto> optMensaje = contactoRepository.findById(id);
        if (optMensaje.isPresent()) {
            MensajeContacto mensaje = optMensaje.get();
            mensaje.setLeido(true);
            return contactoRepository.save(mensaje);
        }
        throw new RuntimeException("Mensaje no encontrado");
    }

    /**
     * Envía una respuesta por email al remitente del mensaje y marca el mensaje como leído.
     * 
     * @param id Identificador del mensaje a responder.
     * @param respuesta Texto de la respuesta redactada por el administrador.
     * @return {@link MensajeContacto} actualizado.
     * @throws RuntimeException si el mensaje no existe o falla el envío.
     */
    public MensajeContacto responderMensaje(Long id, String respuesta) {
        Optional<MensajeContacto> optMensaje = contactoRepository.findById(id);
        if (optMensaje.isPresent()) {
            MensajeContacto mensaje = optMensaje.get();
            
            // Construir y enviar el correo de respuesta
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
