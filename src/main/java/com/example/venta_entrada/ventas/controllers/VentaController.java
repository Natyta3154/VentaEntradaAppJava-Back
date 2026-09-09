package com.example.venta_entrada.ventas.controllers;

import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import com.example.venta_entrada.ventas.dtos.request.CompraRequestDTO;
import com.example.venta_entrada.ventas.dtos.response.CompraResponseDTO;
import com.example.venta_entrada.ventas.dtos.response.EntradaResponseDTO;
import com.example.venta_entrada.ventas.models.Entrada;
import com.example.venta_entrada.ventas.repositories.EntradaRepository;
import com.example.venta_entrada.ventas.services.VentaService;
import com.example.venta_entrada.ventas.utils.QrCodeGeneratorUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/comprar")
    public ResponseEntity<CompraResponseDTO> comprar(@Valid @RequestBody CompraRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        CompraResponseDTO response = ventaService.procesarCompra(request, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-entradas")
    public ResponseEntity<Page<EntradaResponseDTO>> misEntradas(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        Page<Entrada> entradas = entradaRepository.findByCompraUsuarioId(usuario.getId(), pageable);
        
        Page<EntradaResponseDTO> response = entradas.map(e -> 
            EntradaResponseDTO.builder()
                .id(e.getId())
                .eventoNombre(e.getEvento().getNombre())
                .tipoEntradaNombre(e.getTipoEntrada().getNombre())
                .estado(e.getEstado().name())
                .qrCodeBase64(QrCodeGeneratorUtil.generateQrCodeBase64(e.getCodigoQr()))
                .build()
        );
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/test-mp-direct")
    public ResponseEntity<String> testMpDirect() {
        try {
            com.mercadopago.client.preference.PreferenceClient client = new com.mercadopago.client.preference.PreferenceClient();
            java.util.List<com.mercadopago.client.preference.PreferenceItemRequest> items = new java.util.ArrayList<>();
            items.add(com.mercadopago.client.preference.PreferenceItemRequest.builder()
                    .title("Dummy Item")
                    .quantity(1)
                    .unitPrice(new java.math.BigDecimal("100"))
                    .currencyId("ARS")
                    .build());
                    
            com.mercadopago.client.preference.PreferenceBackUrlsRequest backUrls = com.mercadopago.client.preference.PreferenceBackUrlsRequest.builder()
                    .success("https://dihydroxy-adultly-necole.ngrok-free.dev/api/ventas/webhook/success")
                    .build();

            com.mercadopago.client.preference.PreferenceRequest request = com.mercadopago.client.preference.PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .notificationUrl("https://dihydroxy-adultly-necole.ngrok-free.dev/api/ventas/webhook/ipn")
                    .build();

            com.mercadopago.resources.preference.Preference preference = client.create(request);
            return ResponseEntity.ok(preference.getId());
        } catch (com.mercadopago.exceptions.MPApiException apiEx) {
            return ResponseEntity.status(500).body("MP API Error: " + apiEx.getApiResponse().getContent());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }
}
