package com.example.venta_entrada.ventas.services;

import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.ventas.dtos.request.ValidacionQrRequestDTO;
import com.example.venta_entrada.ventas.dtos.response.ValidacionQrResponseDTO;
import com.example.venta_entrada.ventas.models.Entrada;
import com.example.venta_entrada.ventas.models.EstadoEntrada;
import com.example.venta_entrada.ventas.repositories.EntradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccesoService {

    private final EntradaRepository entradaRepository;

    @Transactional
    public ValidacionQrResponseDTO validarAccesoPuerta(ValidacionQrRequestDTO request, Usuario portero) {
        Entrada entrada = entradaRepository.findByCodigoQr(request.getCodigo())
                .orElse(null);

        if (entrada == null) {
            return ValidacionQrResponseDTO.builder()
                    .status("rojo")
                    .mensaje("Entrada inexistente")
                    .build();
        }
        
        ValidacionQrResponseDTO.ValidationDetails detalles = ValidacionQrResponseDTO.ValidationDetails.builder()
                .usuario(entrada.getCompra().getUsuario().getNombre() + " " + entrada.getCompra().getUsuario().getApellido())
                .email(entrada.getCompra().getUsuario().getEmail())
                .tipo(entrada.getTipoEntrada().getNombre())
                .evento(entrada.getEvento().getNombre())
                .build();

        if (entrada.getEvento().getFechaFin() != null && entrada.getEvento().getFechaFin().isBefore(LocalDateTime.now())) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("El evento ya ha finalizado")
                     .detalles(detalles)
                     .build();
        }
        
        if (entrada.getEstado() == EstadoEntrada.USADA) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("Entrada ya utilizada")
                     .detalles(detalles)
                     .build();
        }
        
        if (entrada.getEstado() == EstadoEntrada.CANCELADA) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("Entrada cancelada")
                     .detalles(detalles)
                     .build();
        }

        if (entrada.getEstado() == EstadoEntrada.VALIDA) {
            entrada.setFechaIngreso(LocalDateTime.now());
            entrada.setValidadoPor(portero);
            entrada.setEstado(EstadoEntrada.USADA);
            
            entradaRepository.save(entrada);
            
             return ValidacionQrResponseDTO.builder()
                     .status("verde")
                     .mensaje("Acceso permitido")
                     .detalles(detalles)
                     .build();
        }

        return ValidacionQrResponseDTO.builder()
                .status("rojo")
                .mensaje("Estado de entrada desconocido")
                .detalles(detalles)
                .build();
    }
}
