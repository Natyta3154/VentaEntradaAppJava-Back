package com.example.venta_entrada.eventos.services.impl;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.venta_entrada.core.config.CacheConfig;
import com.example.venta_entrada.eventos.dtos.request.AsignarArtistasRequest;
import com.example.venta_entrada.eventos.dtos.request.CrearEventoRequest;
import com.example.venta_entrada.eventos.dtos.response.ArtistaResponse;
import com.example.venta_entrada.eventos.dtos.response.EventoResponse;
import com.example.venta_entrada.eventos.mappers.EventoMapper;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.eventos.models.EventoArtista;
import com.example.venta_entrada.eventos.repositories.ArtistaRepository;
import com.example.venta_entrada.eventos.repositories.EventoArtistaRepository;
import com.example.venta_entrada.eventos.repositories.EventoRepository;
import com.example.venta_entrada.eventos.services.EventoService;
import com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO;
import com.example.venta_entrada.ventas.dtos.response.TipoEntradaResponseDTO;
import com.example.venta_entrada.ventas.models.TipoEntrada;
import com.example.venta_entrada.ventas.repositories.TipoEntradaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final ArtistaRepository artistaRepository;
    private final EventoArtistaRepository eventoArtistaRepository;
    private final EventoMapper eventoMapper;
    private final TipoEntradaRepository tipoEntradaRepository;

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EVENTOS, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void crearEvento(CrearEventoRequest request) {
        Evento evento = Evento.builder()
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .ubicacion(request.getUbicacion())
            .capacidadTotal(request.getCapacidadTotal())
            .estado(request.getEstado())
            .fechaEvento(request.getFechaEvento())
            .edadMinima(request.getEdadMinima())
            .fechaFin(request.getFechaFin())
            .imagenPortada(request.getImagenPortada())
            .build();    
        eventoRepository.save(evento);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_EVENTOS, key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<EventoResponse> obtenerTodosLosEventos(Pageable pageable) {
        return eventoRepository.findAll(pageable).map(eventoMapper::toResponse);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_EVENTO_DETALLE, key = "#id")
    public Optional<EventoResponse> obtenerEventoPorId(Long id) {
        return eventoRepository.findById(id).map(eventoMapper::toResponse);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EVENTOS, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public EventoResponse actualizarEvento(Long id, CrearEventoRequest request) {
        Evento evento = eventoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontro el evento con el id: " + id));
    
        // Actualizar los campos
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setUbicacion(request.getUbicacion());
        evento.setCapacidadTotal(request.getCapacidadTotal());
        evento.setEstado(request.getEstado());
        evento.setFechaEvento(request.getFechaEvento());
        evento.setEdadMinima(request.getEdadMinima());
        evento.setFechaFin(request.getFechaFin());
        evento.setImagenPortada(request.getImagenPortada());
     
        Evento actualizado = eventoRepository.save(evento);
        return eventoMapper.toResponse(actualizado);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EVENTOS, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void eliminarEvento(Long id) {
        Evento evento = eventoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontro el evento con el id: " + id));
        evento.setActivo(false);
        eventoRepository.save(evento);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_EVENTOS, key = "'search_' + #nombre + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<EventoResponse> buscarEventosPorNombre(String nombre, Pageable pageable) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodosLosEventos(pageable);
        }
        return eventoRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(eventoMapper::toResponse);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_ARTISTAS_EVENTO, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void asignarArtistas(Long eventoId, AsignarArtistasRequest request) {
        // Verificar que el evento exista
        eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el evento con el id: " + eventoId));

        // Iterar sobre los IDs de los artistas enviados
        for (Long artistaId : request.getArtistasIds()) {
            // Verificar que el artista exista
            if (!artistaRepository.existsById(artistaId)) {
                throw new RuntimeException("No se encontro el artista con el id: " + artistaId);
            }

            // Verificar si ya existe la relación para no duplicar
            boolean yaAsignado = eventoArtistaRepository.existsByEventoIdAndArtistaId(eventoId, artistaId);
            if (!yaAsignado) {
                EventoArtista relacion = EventoArtista.builder()
                        .eventoId(eventoId)
                        .artistaId(artistaId)
                        .build();
                eventoArtistaRepository.save(relacion);
            }
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_ARTISTAS_EVENTO, key = "#eventoId")
    public java.util.List<ArtistaResponse> obtenerArtistasPorEvento(Long eventoId) {
        return artistaRepository.findByEventoId(eventoId).stream()
                .map(a -> {
                    ArtistaResponse response = new ArtistaResponse();
                    response.setId(a.getId());
                    response.setNombre(a.getNombre());
                    response.setDescripcion(a.getDescripcion());
                    response.setEstiloMusical(a.getEstiloMusical());
                    response.setFoto(a.getFoto());
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_TIPOS_ENTRADA, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void crearTipoEntrada(Long eventoId, TipoEntradaRequestDTO request) {
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el evento con el id: " + eventoId));
            
        TipoEntrada tipoEntrada = TipoEntrada.builder()
            .evento(evento)
            .nombre(request.getNombre())
            .precio(request.getPrecio())
            .stockTotal(request.getStockTotal())
            .stockDisponible(request.getStockTotal()) // Inicialmente todo disponible
            .fechaInicioVenta(request.getFechaInicioVenta())
            .fechaFinVenta(request.getFechaFinVenta())
            .build();
            
        tipoEntradaRepository.save(tipoEntrada);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_TIPOS_ENTRADA, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void actualizarTipoEntrada(Long eventoId, Long tipoId, TipoEntradaRequestDTO request) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el tipo de entrada con el id: " + tipoId));
        
        if (!tipoEntrada.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("El tipo de entrada no pertenece a este evento");
        }

        int stockDiff = request.getStockTotal() - tipoEntrada.getStockTotal();
        
        tipoEntrada.setNombre(request.getNombre());
        tipoEntrada.setPrecio(request.getPrecio());
        tipoEntrada.setStockTotal(request.getStockTotal());
        tipoEntrada.setStockDisponible(tipoEntrada.getStockDisponible() + stockDiff);
        tipoEntrada.setFechaInicioVenta(request.getFechaInicioVenta());
        tipoEntrada.setFechaFinVenta(request.getFechaFinVenta());
        
        tipoEntradaRepository.save(tipoEntrada);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_TIPOS_ENTRADA, CacheConfig.CACHE_EVENTO_DETALLE}, allEntries = true)
    public void desactivarTipoEntrada(Long eventoId, Long tipoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el tipo de entrada con el id: " + tipoId));
        
        if (!tipoEntrada.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("El tipo de entrada no pertenece a este evento");
        }

        tipoEntrada.setActivo(false);
        tipoEntradaRepository.save(tipoEntrada);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_TIPOS_ENTRADA, key = "#eventoId")
    public java.util.List<TipoEntradaResponseDTO> obtenerTiposEntradaPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        
        return tipoEntradaRepository.findByEventoId(eventoId).stream()
            .map(t -> TipoEntradaResponseDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .precio(t.getPrecio())
                .stockTotal(t.getStockTotal())
                .stockDisponible(t.getStockDisponible())
                .fechaInicioVenta(t.getFechaInicioVenta())
                .fechaFinVenta(t.getFechaFinVenta())
                .activo(t.getActivo())
                .build())
            .toList();
    }
}
