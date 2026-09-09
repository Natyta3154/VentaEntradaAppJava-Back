package com.example.venta_entrada.eventos.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.venta_entrada.eventos.dtos.request.EventoVideoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoVideoResponse;

public interface EventoVideoService {
    void crearVideo(EventoVideoRequest request);
    Page<EventoVideoResponse> obtenerTodosLosVideos(Pageable pageable);
    Optional<EventoVideoResponse> obtenerVideoPorId(Long id);
    EventoVideoResponse actualizarVideo(Long id, EventoVideoRequest request);
    void eliminarVideo(Long id);
}
