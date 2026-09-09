package com.example.venta_entrada.eventos.services;

import java.util.Optional;

import com.example.venta_entrada.eventos.dtos.request.EventoImagenRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoImagenResponse;

/**
 * Interfaz que define los servicios para la gestión de imágenes de eventos.
 */
public interface EventoImagenService {

    /**
     * Crea una nueva imagen en el sistema.
     * 
     * @param request Objeto DTO que contiene la información de la imagen.
     */
    void crearImagen(EventoImagenRequest request);
    
    /**
     * Obtiene una lista paginada de todas las imágenes registradas.
     * 
     * @param pageable Configuración de paginación.
     * @return Página de objetos DTO de imágenes.
     */
    org.springframework.data.domain.Page<EventoImagenResponse> obtenerTodasLasImagenes(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Busca una imagen por su identificador único.
     * 
     * @param id Identificador de la imagen.
     * @return Un Optional que contiene el DTO de la imagen si se encuentra, o vacío en caso contrario.
     */
    Optional<EventoImagenResponse> obtenerImagenPorId(Long id);
    
    /**
     * Actualiza la información de una imagen existente.
     * 
     * @param id Identificador de la imagen a actualizar.
     * @param request Objeto DTO con los datos actualizados.
     * @return El objeto DTO de la imagen ya actualizada.
     */
    EventoImagenResponse actualizarImagen(Long id, EventoImagenRequest request);
    
    /**
     * Elimina una imagen del sistema.
     * 
     * @param id Identificador de la imagen a eliminar.
     */
    void eliminarImagen(Long id);
}
