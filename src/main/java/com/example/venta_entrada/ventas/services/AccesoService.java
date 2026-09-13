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

/**
 * Servicio de control de accesos y validación de entradas por código QR en puertas.
 * 
 * Reglas de negocio que valida:
 * 1. La entrada debe existir en la base de datos (por su UUID/Código QR).
 * 2. El evento no debe haber finalizado según su 'fechaFin'.
 * 3. La entrada no debe haber sido usada previamente (previene fraude o duplicación).
 * 4. La entrada no debe estar en estado 'CANCELADA' (ej: por reembolso).
 * 5. Si la entrada está en estado 'VALIDA': se registra fecha de ingreso, el usuario portero y pasa a estado 'USADA' (Acceso Verde).
 */
@Service
@RequiredArgsConstructor
public class AccesoService {

    private final EntradaRepository entradaRepository;

    /**
     * Valida el código QR escaneado en el acceso del evento.
     * 
     * @param request DTO que contiene el código UUID escaneado del QR.
     * @param portero Usuario con rol STAFF/PORTERO/ADMIN que realiza la validación en la puerta.
     * @return {@link ValidacionQrResponseDTO} con estado visual ("verde" o "rojo"), mensaje explicativo y detalles del asistente.
     */
    @Transactional
    public ValidacionQrResponseDTO validarAccesoPuerta(ValidacionQrRequestDTO request, Usuario portero) {
        // Buscar la entrada por el código único UUID del QR
        Entrada entrada = entradaRepository.findByCodigoQr(request.getCodigo())
                .orElse(null);

        // 1. Validar existencia
        if (entrada == null) {
            return ValidacionQrResponseDTO.builder()
                    .status("rojo")
                    .mensaje("Entrada inexistente")
                    .build();
        }
        
        // Construir detalles del asistente y evento para mostrar en la pantalla del portero
        ValidacionQrResponseDTO.ValidationDetails detalles = ValidacionQrResponseDTO.ValidationDetails.builder()
                .usuario(entrada.getCompra().getUsuario().getNombre() + " " + entrada.getCompra().getUsuario().getApellido())
                .email(entrada.getCompra().getUsuario().getEmail())
                .tipo(entrada.getTipoEntrada().getNombre())
                .evento(entrada.getEvento().getNombre())
                .build();

        // 2. Validar que el evento siga vigente
        if (entrada.getEvento().getFechaFin() != null && entrada.getEvento().getFechaFin().isBefore(LocalDateTime.now())) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("El evento ya ha finalizado")
                     .detalles(detalles)
                     .build();
        }
        
        // 3. Validar si ya fue utilizada anteriormente
        if (entrada.getEstado() == EstadoEntrada.USADA) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("Entrada ya utilizada")
                     .detalles(detalles)
                     .build();
        }
        
        // 4. Validar si fue cancelada
        if (entrada.getEstado() == EstadoEntrada.CANCELADA) {
             return ValidacionQrResponseDTO.builder()
                     .status("rojo")
                     .mensaje("Entrada cancelada")
                     .detalles(detalles)
                     .build();
        }

        // 5. Entrada válida: permitir ingreso y marcarla como USADA
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

        // Caso por defecto (ej: PENDIENTE o RESERVADA)
        return ValidacionQrResponseDTO.builder()
                .status("rojo")
                .mensaje("Estado de entrada desconocido")
                .detalles(detalles)
                .build();
    }
}

