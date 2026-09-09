package com.example.venta_entrada.eventos.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.eventos.dtos.request.AsignarArtistasRequest;
import com.example.venta_entrada.eventos.dtos.request.CrearEventoRequest;
import com.example.venta_entrada.eventos.dtos.response.EventoResponse;
import com.example.venta_entrada.eventos.mappers.EventoMapper;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.eventos.models.EventoArtista;
import com.example.venta_entrada.eventos.repositories.ArtistaRepository;
import com.example.venta_entrada.eventos.repositories.EventoArtistaRepository;
import com.example.venta_entrada.eventos.repositories.EventoRepository;
import com.example.venta_entrada.eventos.services.EventoService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final ArtistaRepository artistaRepository;
    private final EventoArtistaRepository eventoArtistaRepository;
    private final EventoMapper eventoMapper;
    private final com.example.venta_entrada.ventas.repositories.TipoEntradaRepository tipoEntradaRepository;

    @Override
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
    public Page<EventoResponse> obtenerTodosLosEventos(Pageable pageable){
        return eventoRepository.findAll(pageable).map(eventoMapper::toResponse);
    }

    @Override
    public Optional<EventoResponse> obtenerEventoPorId(Long id){
        return eventoRepository.findById(id).map(eventoMapper::toResponse);
    }

    @Override
    public EventoResponse actualizarEvento(Long id, CrearEventoRequest request){

        Evento evento = eventoRepository.findById(id)
            .orElseThrow(()-> new RuntimeException("No se encontro el evento con el id: " + id));
    
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
    public void eliminarEvento(Long id){
        Evento evento = eventoRepository.findById(id).orElseThrow(()-> new RuntimeException("No se encontro el evento con el id: " + id));
        evento.setActivo(false);
        eventoRepository.save(evento);
    }

    @Override
    public Page<EventoResponse> buscarEventosPorNombre(String nombre, Pageable pageable){
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodosLosEventos(pageable);
        }
        return eventoRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(eventoMapper::toResponse);
    }

    @Override
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
    public java.util.List<com.example.venta_entrada.eventos.dtos.response.ArtistaResponse> obtenerArtistasPorEvento(Long eventoId) {
        return artistaRepository.findByEventoId(eventoId).stream()
                .map(a -> {
                    com.example.venta_entrada.eventos.dtos.response.ArtistaResponse response = new com.example.venta_entrada.eventos.dtos.response.ArtistaResponse();
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
    public void crearTipoEntrada(Long eventoId, com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request) {
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el evento con el id: " + eventoId));
            
        com.example.venta_entrada.ventas.models.TipoEntrada tipoEntrada = com.example.venta_entrada.ventas.models.TipoEntrada.builder()
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
    public void actualizarTipoEntrada(Long eventoId, Long tipoId, com.example.venta_entrada.ventas.dtos.request.TipoEntradaRequestDTO request) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        com.example.venta_entrada.ventas.models.TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el tipo de entrada con el id: " + tipoId));
        
        if (!tipoEntrada.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("El tipo de entrada no pertenece a este evento");
        }

        // We update stockDisponible based on stockTotal change if needed, but for simplicity, let's just update stockTotal.
        int stockDiff = request.getStockTotal() - tipoEntrada.getStockTotal();
        
        tipoEntrada.setNombre(request.getNombre());
        tipoEntrada.setPrecio(request.getPrecio());
        tipoEntrada.setStockTotal(request.getStockTotal());
        // Ajustamos el stock disponible si se aumenta o disminuye el total
        tipoEntrada.setStockDisponible(tipoEntrada.getStockDisponible() + stockDiff);
        tipoEntrada.setFechaInicioVenta(request.getFechaInicioVenta());
        tipoEntrada.setFechaFinVenta(request.getFechaFinVenta());
        
        tipoEntradaRepository.save(tipoEntrada);
    }

    @Override
    public void desactivarTipoEntrada(Long eventoId, Long tipoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        com.example.venta_entrada.ventas.models.TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoId)
            .orElseThrow(() -> new RuntimeException("No se encontro el tipo de entrada con el id: " + tipoId));
        
        if (!tipoEntrada.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("El tipo de entrada no pertenece a este evento");
        }

        tipoEntrada.setActivo(false);
        tipoEntradaRepository.save(tipoEntrada);
    }

    @Override
    public java.util.List<com.example.venta_entrada.ventas.dtos.response.TipoEntradaResponseDTO> obtenerTiposEntradaPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RuntimeException("No se encontro el evento con el id: " + eventoId);
        }
        
        return tipoEntradaRepository.findByEventoId(eventoId).stream()
            .map(t -> com.example.venta_entrada.ventas.dtos.response.TipoEntradaResponseDTO.builder()
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


