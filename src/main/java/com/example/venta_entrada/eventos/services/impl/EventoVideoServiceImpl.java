package com.example.venta_entrada.eventos.services.impl;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.eventos.dtos.request.EventoVideoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoVideoResponse;
import com.example.venta_entrada.eventos.mappers.EventoVideoMapper;
import com.example.venta_entrada.eventos.models.EventoVideo;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.eventos.repositories.EventoVideoRepository;
import com.example.venta_entrada.eventos.repositories.EventoRepository;
import com.example.venta_entrada.eventos.services.EventoVideoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoVideoServiceImpl implements EventoVideoService {

    private final EventoVideoRepository eventoVideoRepository;
    private final EventoRepository eventoRepository;
    private final EventoVideoMapper eventoVideoMapper;

    @Override
    public void crearVideo(EventoVideoRequest request) {
        Evento evento = eventoRepository.findById(request.getEventoId())
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con id: " + request.getEventoId()));

        EventoVideo eventoVideo = EventoVideo.builder()
            .nombreVideo(request.getNombreVideo())
            .urlVideo(request.getUrlVideo())
            .orden(request.getOrden())
            .evento(evento)
            .build();    
            
        eventoVideoRepository.save(eventoVideo);
    }

    @Override
    public Page<EventoVideoResponse> obtenerTodosLosVideos(Pageable pageable) {
        return eventoVideoRepository.findAll(pageable)
            .map(eventoVideoMapper::toResponse);
    }

    @Override
    public Optional<EventoVideoResponse> obtenerVideoPorId(Long id) {
        return eventoVideoRepository.findById(id)
            .map(eventoVideoMapper::toResponse);
    }

    @Override
    public EventoVideoResponse actualizarVideo(Long id, EventoVideoRequest request) {
        EventoVideo eventoVideo = eventoVideoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Video no encontrado con id: " + id));
            
        Evento evento = eventoRepository.findById(request.getEventoId())
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con id: " + request.getEventoId()));
            
        eventoVideo.setNombreVideo(request.getNombreVideo());
        eventoVideo.setUrlVideo(request.getUrlVideo());
        eventoVideo.setOrden(request.getOrden());
        eventoVideo.setEvento(evento);
        
        EventoVideo actualizado = eventoVideoRepository.save(eventoVideo);
        return eventoVideoMapper.toResponse(actualizado);
    }

    @Override
    public void eliminarVideo(Long id) {
        eventoVideoRepository.deleteById(id);
    }
}
