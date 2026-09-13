package com.example.venta_entrada.contacto.controllers;

import com.example.venta_entrada.contacto.dtos.ContactoRequestDTO;
import com.example.venta_entrada.contacto.dtos.ContactoRespuestaDTO;
import com.example.venta_entrada.contacto.models.MensajeContacto;
import com.example.venta_entrada.contacto.services.ContactoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    @PostMapping
    public ResponseEntity<MensajeContacto> enviarMensaje(@Valid @RequestBody ContactoRequestDTO requestDTO) {
        MensajeContacto mensajeGuardado = contactoService.guardarMensaje(requestDTO);
        return new ResponseEntity<>(mensajeGuardado, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MensajeContacto>> obtenerContactos() {
        return ResponseEntity.ok(contactoService.obtenerTodos());
    }

    @PutMapping("/{id}/leido")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeContacto> marcarComoLeido(@PathVariable Long id) {
        return ResponseEntity.ok(contactoService.marcarComoLeido(id));
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeContacto> responderMensaje(
            @PathVariable Long id, 
            @Valid @RequestBody ContactoRespuestaDTO respuestaDTO) {
        return ResponseEntity.ok(contactoService.responderMensaje(id, respuestaDTO.getRespuesta()));
    }
}
