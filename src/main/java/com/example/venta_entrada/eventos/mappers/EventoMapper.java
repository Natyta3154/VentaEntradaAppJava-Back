package com.example.venta_entrada.eventos.mappers;

import org.mapstruct.Mapper;
import com.example.venta_entrada.eventos.dtos.response.EventoResponse;
import com.example.venta_entrada.eventos.models.Evento;

import org.mapstruct.Mapping;
import com.example.venta_entrada.ventas.models.TipoEntrada;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface EventoMapper {
    @Mapping(source = "tiposEntrada", target = "tipos_tickets")
    EventoResponse toResponse(Evento evento);

    @Mapping(source = "stockDisponible", target = "stock_disponible")
    EventoResponse.TipoEntradaDTO toTipoEntradaDTO(TipoEntrada tipoEntrada);
}
