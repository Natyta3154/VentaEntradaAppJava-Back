package com.example.venta_entrada.usuarios.services;
import com.example.venta_entrada.usuarios.dtos.request.RegistroUsuarioRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.venta_entrada.usuarios.dtos.request.CambiarRolRequest;
import com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse;

public interface UsuarioService {

    void registrarUsuario(RegistroUsuarioRequest request);

    Page<UsuarioResponse> obtenerUsuarios(Pageable pageable);

    UsuarioResponse cambiarRol(Long id, CambiarRolRequest request);

    UsuarioResponse editarUsuario(Long id, com.example.venta_entrada.usuarios.dtos.request.EditarUsuarioRequest request);

    void eliminarUsuario(Long id);
}
