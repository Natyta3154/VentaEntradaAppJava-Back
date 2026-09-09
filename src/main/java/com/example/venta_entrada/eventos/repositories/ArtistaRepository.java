package com.example.venta_entrada.eventos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.venta_entrada.eventos.models.Artista;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {
    org.springframework.data.domain.Page<Artista> findByNombreContainingIgnoreCase(String nombre, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Artista a JOIN EventoArtista ea ON a.id = ea.artistaId WHERE ea.eventoId = :eventoId")
    java.util.List<Artista> findByEventoId(@org.springframework.data.repository.query.Param("eventoId") Long eventoId);
}
