package com.example.venta_entrada.eventos.mappers;

import org.mapstruct.Mapper;

import com.example.venta_entrada.eventos.dtos.response.EventoImagenResponse;
import com.example.venta_entrada.eventos.models.EventoImagen;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface EventoImagenMapper {

    EventoImagenResponse toResponse(EventoImagen entity);

}
