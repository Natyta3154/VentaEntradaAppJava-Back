package com.example.venta_entrada.eventos.dtos.request;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AsignarArtistasRequest {

    @NotEmpty(message = "La lista de artistas no puede estar vacía")
    private List<Long> artistasIds;

}
