package com.example.venta_entrada.eventos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;  

import com.example.venta_entrada.eventos.models.EventoImagen;

public interface EventoImagenRepository extends JpaRepository<EventoImagen, Long>{ 

    List<EventoImagen> findByEventoIdOrderByOrdenAsc(Long eventoId);

}
