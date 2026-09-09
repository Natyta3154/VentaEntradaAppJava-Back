package com.example.venta_entrada.eventos.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.venta_entrada.eventos.dtos.request.EventoVideoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoVideoResponse;
import com.example.venta_entrada.eventos.services.EventoVideoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Videos de Eventos", description = "Endpoints para la gestión de videos de los eventos")
public class VideoControllers {

    private final EventoVideoService eventoVideoService;

    @Operation(summary = "Crear un nuevo video")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> crearVideo(@Valid @RequestBody EventoVideoRequest request) {
        eventoVideoService.crearVideo(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los videos con paginación")
    @GetMapping("/todos")
    public ResponseEntity<Page<EventoVideoResponse>> obtenerTodosLosVideos(Pageable pageable) {
        Page<EventoVideoResponse> videos = eventoVideoService.obtenerTodosLosVideos(pageable);
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @Operation(summary = "Obtener un video por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventoVideoResponse> obtenerVideoPorId(@PathVariable Long id) {
        return eventoVideoService.obtenerVideoPorId(id)
                .map(video -> new ResponseEntity<>(video, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Actualizar un video existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoVideoResponse> actualizarVideo(@PathVariable Long id, @Valid @RequestBody EventoVideoRequest request) {
        try {
            EventoVideoResponse response = eventoVideoService.actualizarVideo(id, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Eliminar un video lógicamente")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarVideo(@PathVariable Long id) {
        eventoVideoService.eliminarVideo(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
