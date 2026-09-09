package com.example.venta_entrada.eventos.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.venta_entrada.eventos.dtos.request.CrearArtistaRequest;
import com.example.venta_entrada.eventos.dtos.response.ArtistaResponse;

public interface ArtistaService {

    void crearArtista(CrearArtistaRequest request);
    
    Page<ArtistaResponse> obtenerTodosLosArtistas(Pageable pageable);
    
    Optional<ArtistaResponse> obtenerArtistaPorId(Long id);
    
    ArtistaResponse actualizarArtista(Long id, CrearArtistaRequest request);
    
    void eliminarArtista(Long id);
    
    Page<ArtistaResponse> buscarArtistasPorNombre(String nombre, Pageable pageable);

}
