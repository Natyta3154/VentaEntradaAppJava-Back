package com.example.venta_entrada.eventos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.venta_entrada.eventos.models.EventoArtista;

@Repository
public interface EventoArtistaRepository extends JpaRepository<EventoArtista, Long> {
    boolean existsByEventoIdAndArtistaId(Long eventoId, Long artistaId);
}
