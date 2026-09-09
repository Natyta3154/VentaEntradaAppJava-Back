package com.example.venta_entrada.eventos.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.venta_entrada.eventos.dtos.request.CrearArtistaRequest;
import com.example.venta_entrada.eventos.dtos.response.ArtistaResponse;
import com.example.venta_entrada.eventos.services.ArtistaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/artistas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Artistas", description = "Endpoints para la gestión de los artistas que asisten a los eventos")
public class ArtistaController {

    private final ArtistaService artistaService;

    @Operation(summary = "Obtener todos los artistas o buscar por nombre (con paginación)")
    @GetMapping
    public ResponseEntity<Page<ArtistaResponse>> obtenerArtistas(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<ArtistaResponse> artistas = artistaService.buscarArtistasPorNombre(nombre, pageable);
        return new ResponseEntity<>(artistas, HttpStatus.OK);
    }
    
    @Operation(summary = "Obtener un artista por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArtistaResponse> obtenerArtistaPorId(@PathVariable Long id) {
        return artistaService.obtenerArtistaPorId(id)
                .map(artista -> new ResponseEntity<>(artista, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Crear un nuevo artista")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> crearArtista(@Valid @RequestBody CrearArtistaRequest request) {
        artistaService.crearArtista(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
        
    @Operation(summary = "Actualizar un artista existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistaResponse> actualizarArtista(
        @PathVariable Long id,
        @Valid @RequestBody CrearArtistaRequest request) {
        try {
            ArtistaResponse artistaActualizado = artistaService.actualizarArtista(id, request);
            return new ResponseEntity<>(artistaActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @Operation(summary = "Eliminar (soft delete) un artista")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarArtista(@PathVariable Long id) {
        artistaService.eliminarArtista(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
