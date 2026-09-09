package com.example.venta_entrada.eventos.mappers;

import org.mapstruct.Mapper;
import com.example.venta_entrada.eventos.dtos.response.ArtistaResponse;
import com.example.venta_entrada.eventos.models.Artista;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ArtistaMapper {
    ArtistaResponse toResponse(Artista entity);
}
