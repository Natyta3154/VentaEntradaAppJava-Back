package com.example.venta_entrada.eventos.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.venta_entrada.eventos.dtos.request.EventoImagenRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoImagenResponse;
import com.example.venta_entrada.eventos.services.EventoImagenService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador REST para gestionar las operaciones CRUD de las imágenes de eventos.
 * Proporciona endpoints para crear, leer, actualizar y eliminar imágenes.
 */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenControllers {

    private final EventoImagenService eventoImagenService;

    /**
     * Obtiene una lista paginada con todas las imágenes registradas en el sistema.
     * 
     * @param page Número de página (empezando en 0)
     * @param size Tamaño de la página
     * @param sortBy Campo por el cual ordenar
     * @return ResponseEntity con la página de imágenes (HTTP 200 OK).
     */
    @GetMapping("/todas")
    public ResponseEntity<Page<EventoImagenResponse>> obtenerTodasLasImagenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy){
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<EventoImagenResponse> imagenes = eventoImagenService.obtenerTodasLasImagenes(pageable);
        return ResponseEntity.ok(imagenes);
    }   

    /**
     * Busca una imagen específica por su ID.
     * 
     * @param id El ID de la imagen a buscar.
     * @return ResponseEntity con la imagen si se encuentra (HTTP 200 OK), 
     *         o un estado HTTP 404 Not Found si no existe.
     */
    @GetMapping("/imagen/{id}")
    public ResponseEntity<EventoImagenResponse> obtenerImagenPorId(@PathVariable Long id){
        return eventoImagenService.obtenerImagenPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Actualiza los datos de una imagen existente. 
     * Requiere permisos de administrador.
     * 
     * @param id El ID de la imagen a actualizar.
     * @param request El objeto DTO con los nuevos datos de la imagen.
     * @return ResponseEntity con la imagen actualizada (HTTP 200 OK).
     */
    @PutMapping("/actualizarImagen/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoImagenResponse> actualizarImagen(@PathVariable Long id, @RequestBody EventoImagenRequest request){
        return ResponseEntity.ok(eventoImagenService.actualizarImagen(id, request));
    }

   /**
    * Elimina una imagen del sistema por su ID.
    * Requiere permisos de administrador.
    * 
    * @param id El ID de la imagen a eliminar.
    * @return ResponseEntity vacío con estado HTTP 204 No Content.
    */
   @DeleteMapping("/eliminarImagen/{id}")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Void> eliminarImagen(@PathVariable Long id){
        eventoImagenService.eliminarImagen(id);
        return ResponseEntity.noContent().build();
   }

    /**
     * Crea una nueva imagen para un evento.
     * Requiere permisos de administrador.
     * 
     * @param request El objeto DTO con la información de la imagen a crear.
     * @return ResponseEntity con un mensaje de éxito y estado HTTP 201 Created.
     */
    @PostMapping("/crearImagen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> crearImagen(@RequestBody EventoImagenRequest request){
        eventoImagenService.crearImagen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Imagen creada exitosamente!");
    }

}
