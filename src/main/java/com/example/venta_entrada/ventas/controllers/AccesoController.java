package com.example.venta_entrada.ventas.controllers;

import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import com.example.venta_entrada.ventas.dtos.request.ValidacionQrRequestDTO;
import com.example.venta_entrada.ventas.dtos.response.ValidacionQrResponseDTO;
import com.example.venta_entrada.ventas.services.AccesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entradas")
@RequiredArgsConstructor
public class AccesoController {

    private final AccesoService accesoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/validar-qr")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('PORTERO') or hasRole('ADMIN')")
    public ResponseEntity<ValidacionQrResponseDTO> validarQr(@Valid @RequestBody ValidacionQrRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario portero = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario portero no encontrado"));
                
        ValidacionQrResponseDTO response = accesoService.validarAccesoPuerta(request, portero);
        return ResponseEntity.ok(response);
    }
}
