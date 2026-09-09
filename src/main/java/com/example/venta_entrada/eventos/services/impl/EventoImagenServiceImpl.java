package com.example.venta_entrada.eventos.services.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.venta_entrada.eventos.dtos.request.EventoImagenRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoImagenResponse;
import com.example.venta_entrada.eventos.models.EventoImagen;
import com.example.venta_entrada.eventos.repositories.EventoImagenRepository;
import com.example.venta_entrada.eventos.services.EventoImagenService;
import com.example.venta_entrada.eventos.mappers.EventoImagenMapper;

import lombok.RequiredArgsConstructor;

/**
 * Implementación de los servicios para la gestión de imágenes de eventos.
 * Proporciona la lógica de negocio y comunicación con la base de datos.
 */
@Service
@RequiredArgsConstructor
public class EventoImagenServiceImpl implements EventoImagenService {

    private final EventoImagenRepository eventoImagenRepository;
    private final EventoImagenMapper eventoImagenMapper;
    private final com.example.venta_entrada.eventos.repositories.EventoRepository eventoRepository;

    @Override
    public void crearImagen(EventoImagenRequest request) {
        com.example.venta_entrada.eventos.models.Evento evento = eventoRepository.findById(request.getEventoId())
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con id: " + request.getEventoId()));

        EventoImagen eventoImagen = EventoImagen.builder()
            .nombreImagen(request.getNombreImagen())
            .urlImagen(request.getUrlImagen())
            .orden(request.getOrden())
            .evento(evento)
            .build();    
            
        eventoImagenRepository.save(eventoImagen);
    }

    @Override
    public org.springframework.data.domain.Page<EventoImagenResponse> obtenerTodasLasImagenes(org.springframework.data.domain.Pageable pageable) {
        return eventoImagenRepository.findAll(pageable)
            .map(eventoImagenMapper::toResponse);
    }

    @Override
    public Optional<EventoImagenResponse> obtenerImagenPorId(Long id) {
        return eventoImagenRepository.findById(id)
            .map(eventoImagenMapper::toResponse);
    }

    @Override
    public EventoImagenResponse actualizarImagen(Long id, EventoImagenRequest request) {
        EventoImagen eventoImagen = eventoImagenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));
            
        eventoImagen.setNombreImagen(request.getNombreImagen());
        eventoImagen.setUrlImagen(request.getUrlImagen());
        eventoImagen.setOrden(request.getOrden());
        
        EventoImagen actualizada = eventoImagenRepository.save(eventoImagen);
        return eventoImagenMapper.toResponse(actualizada);
    }

    @Override
    public void eliminarImagen(Long id) {
        eventoImagenRepository.deleteById(id);
    }
    
}
