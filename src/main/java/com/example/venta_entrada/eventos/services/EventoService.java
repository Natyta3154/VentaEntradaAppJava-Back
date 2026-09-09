package com.example.venta_entrada.eventos.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.venta_entrada.eventos.dtos.request.AsignarArtistasRequest;
import com.example.venta_entrada.eventos.dtos.request.CrearEventoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoResponse;

public interface EventoService {

    void crearEvento(CrearEventoRequest request);
    
    Page<EventoResponse> obtenerTodosLosEventos(Pageable pageable);
    
    Optional<EventoResponse> obtenerEventoPorId(Long id);
    
    EventoResponse actualizarEvento(Long id, CrearEventoRequest request);
    
    void eliminarEvento(Long id);
    
    Page<EventoResponse> buscarEventosPorNombre(String nombre, Pageable pageable);

    void asignarArtistas(Long eventoId, AsignarArtistasRequest request);

    java.util.List<com.example.venta_entrada.eventos.dtos.response.ArtistaResponse> obtenerArtistasPorEvento(Long eventoId);

    void crearTipoEntrada(Long eventoId, com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request);

    void actualizarTipoEntrada(Long eventoId, Long tipoId, com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request);

    void desactivarTipoEntrada(Long eventoId, Long tipoId);

    java.util.List<com.example.venta_entrada.ventas.dtos.response.TipoEntradaResponseDTO> obtenerTiposEntradaPorEvento(Long eventoId);

}
