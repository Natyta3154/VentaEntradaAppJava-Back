package com.example.venta_entrada.eventos.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.venta_entrada.eventos.dtos.request.AsignarArtistasRequest;
import com.example.venta_entrada.eventos.dtos.request.CrearEventoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoResponse;
import com.example.venta_entrada.eventos.services.EventoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Eventos Principales", description = "Endpoints para la gestión central de eventos")
public class EventoController {

    private final EventoService eventoService;

    @Operation(summary = "Obtener todos los eventos o buscar por nombre (con paginación)")
    @GetMapping
    public ResponseEntity<Page<EventoResponse>> obtenerEventos(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EventoResponse> eventos = eventoService.buscarEventosPorNombre(nombre, pageable);
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }
    
    @Operation(summary = "Obtener un evento por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> obtenerEventoPorId(@PathVariable Long id) {
        return eventoService.obtenerEventoPorId(id)
                .map(evento -> new ResponseEntity<>(evento, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Crear un nuevo evento")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> crearEvento(@Valid @RequestBody CrearEventoRequest request) {
        eventoService.crearEvento(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
        
    @Operation(summary = "Actualizar un evento existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoResponse> actualizarEvento(
        @PathVariable Long id,
        @Valid @RequestBody CrearEventoRequest request) {
        try {
            EventoResponse eventoActualizado = eventoService.actualizarEvento(id, request);
            return new ResponseEntity<>(eventoActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @Operation(summary = "Eliminar (soft delete) un evento")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarEvento(@PathVariable Long id) {
        eventoService.eliminarEvento(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @Operation(summary = "Asignar artistas a un evento")
    @PostMapping("/{id}/artistas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> asignarArtistas(@PathVariable Long id, @Valid @RequestBody AsignarArtistasRequest request) {
        eventoService.asignarArtistas(id, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Obtener los artistas asignados a un evento")
    @GetMapping("/{id}/artistas")
    public ResponseEntity<java.util.List<com.example.venta_entrada.eventos.dtos.response.ArtistaResponse>> obtenerArtistasPorEvento(@PathVariable Long id) {
        return new ResponseEntity<>(eventoService.obtenerArtistasPorEvento(id), org.springframework.http.HttpStatus.OK);
    }

    @Operation(summary = "Obtener los tipos de entrada de un evento")
    @GetMapping("/{id}/tipos-entrada")
    public ResponseEntity<java.util.List<com.example.venta_entrada.ventas.dtos.response.TipoEntradaResponseDTO>> obtenerTiposEntrada(@PathVariable Long id) {
        return new ResponseEntity<>(eventoService.obtenerTiposEntradaPorEvento(id), HttpStatus.OK);
    }

    @Operation(summary = "Crear un nuevo tipo de entrada para un evento")
    @PostMapping("/{id}/tipos-entrada")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> crearTipoEntrada(
            @PathVariable Long id,
            @Valid @RequestBody com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request) {
        eventoService.crearTipoEntrada(id, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un tipo de entrada de un evento")
    @PutMapping("/{id}/tipos-entrada/{tipoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> actualizarTipoEntrada(
            @PathVariable Long id,
            @PathVariable Long tipoId,
            @Valid @RequestBody com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request) {
        eventoService.actualizarTipoEntrada(id, tipoId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Desactivar un tipo de entrada (Soft delete)")
    @PatchMapping("/{id}/tipos-entrada/{tipoId}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarTipoEntrada(
            @PathVariable Long id,
            @PathVariable Long tipoId) {
        eventoService.desactivarTipoEntrada(id, tipoId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

