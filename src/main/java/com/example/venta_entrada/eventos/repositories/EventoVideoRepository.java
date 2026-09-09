package com.example.venta_entrada.eventos.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.venta_entrada.eventos.models.EventoVideo;

public interface EventoVideoRepository extends JpaRepository<EventoVideo, Long> {
    List<EventoVideo> findByEventoIdOrderByOrdenAsc(Long eventoId);
}
