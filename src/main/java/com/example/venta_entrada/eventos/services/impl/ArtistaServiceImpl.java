package com.example.venta_entrada.eventos.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.eventos.dtos.request.CrearArtistaRequest;
import com.example.venta_entrada.eventos.dtos.response.ArtistaResponse;
import com.example.venta_entrada.eventos.mappers.ArtistaMapper;
import com.example.venta_entrada.eventos.models.Artista;
import com.example.venta_entrada.eventos.repositories.ArtistaRepository;
import com.example.venta_entrada.eventos.services.ArtistaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArtistaServiceImpl implements ArtistaService {

    private final ArtistaRepository artistaRepository;
    private final ArtistaMapper artistaMapper;

    @Override
    public void crearArtista(CrearArtistaRequest request) {
        Artista artista = Artista.builder()
            .nombre(request.getNombre())
            .estiloMusical(request.getEstiloMusical())
            .descripcion(request.getDescripcion())
            .foto(request.getFoto())
            .fechaCreacion(LocalDateTime.now())
            .build();    
            
        artistaRepository.save(artista);
    }

    @Override
    public Page<ArtistaResponse> obtenerTodosLosArtistas(Pageable pageable) {
        return artistaRepository.findAll(pageable).map(artistaMapper::toResponse);
    }

    @Override
    public Optional<ArtistaResponse> obtenerArtistaPorId(Long id) {
        return artistaRepository.findById(id).map(artistaMapper::toResponse);
    }

    @Override
    public ArtistaResponse actualizarArtista(Long id, CrearArtistaRequest request) {
        Artista artista = artistaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontro el artista con el id: " + id));

        artista.setNombre(request.getNombre());
        artista.setEstiloMusical(request.getEstiloMusical());
        artista.setDescripcion(request.getDescripcion());
        artista.setFoto(request.getFoto());
        
        Artista actualizado = artistaRepository.save(artista);
        return artistaMapper.toResponse(actualizado);
    }

    @Override
    public void eliminarArtista(Long id) {
        artistaRepository.deleteById(id);
    }

    @Override
    public Page<ArtistaResponse> buscarArtistasPorNombre(String nombre, Pageable pageable) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodosLosArtistas(pageable);
        }
        return artistaRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(artistaMapper::toResponse);
    }
}
