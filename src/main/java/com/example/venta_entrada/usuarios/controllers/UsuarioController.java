package com.example.venta_entrada.usuarios.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.venta_entrada.usuarios.dtos.request.RegistroUsuarioRequest;
import com.example.venta_entrada.usuarios.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request){
        usuarioService.registrarUsuario(request);

        return ResponseEntity
               .status(HttpStatus.CREATED)
               .body("Usuario registrado exitosamente!");
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse>> obtenerUsuarios(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios(pageable));
    }

    @PutMapping("/{id}/rol")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse> cambiarRol(
            @PathVariable Long id, 
            @Valid @RequestBody com.example.venta_entrada.usuarios.dtos.request.CambiarRolRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse> editarUsuario(
            @PathVariable Long id, 
            @Valid @RequestBody com.example.venta_entrada.usuarios.dtos.request.EditarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.editarUsuario(id, request));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
