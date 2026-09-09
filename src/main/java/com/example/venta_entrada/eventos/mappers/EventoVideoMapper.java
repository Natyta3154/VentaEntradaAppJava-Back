package com.example.venta_entrada.eventos.mappers;

import org.mapstruct.Mapper;
import com.example.venta_entrada.eventos.dtos.response.EventoVideoResponse;
import com.example.venta_entrada.eventos.models.EventoVideo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface EventoVideoMapper {
    EventoVideoResponse toResponse(EventoVideo entity);
}
