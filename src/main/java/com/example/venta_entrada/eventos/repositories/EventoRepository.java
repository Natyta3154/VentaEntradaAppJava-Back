package com.example.venta_entrada.eventos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.venta_entrada.eventos.models.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    org.springframework.data.domain.Page<Evento> findByNombreContainingIgnoreCase(String nombre, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Evento e WHERE e.id = :id")
    java.util.Optional<Evento> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);
}
