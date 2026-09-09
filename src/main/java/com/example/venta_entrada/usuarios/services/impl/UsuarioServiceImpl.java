package com.example.venta_entrada.usuarios.services.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.usuarios.dtos.request.RegistroUsuarioRequest;
import com.example.venta_entrada.usuarios.models.Rol;
import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.usuarios.repositories.RolRepository;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;
import com.example.venta_entrada.usuarios.services.UsuarioService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de usuarios.
 * Encargado de manejar la lógica de negocio relacionada con los usuarios,
 * como por ejemplo el registro de nuevos usuarios en el sistema.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

   private final UsuarioRepository usuarioRepository;
   private final RolRepository rolRepository;
   private final PasswordEncoder passwordEncoder;

   /**
    * Registra un nuevo usuario en el sistema con el rol por defecto de 'ROLE_CLIENTE'.
    * 
    * @param request DTO que contiene los datos de registro (nombre, apellido, email, contraseña).
    * @throws com.example.venta_entrada.exceptions.EmailYaRegistradoException si el email ya existe en la base de datos.
    * @throws RuntimeException si el rol 'ROLE_CLIENTE' no está configurado en la base de datos.
    */
   @Override
   public void registrarUsuario(RegistroUsuarioRequest request){
    if (usuarioRepository.existsByEmail(request.email())){
        throw new com.example.venta_entrada.core.exceptions.EmailYaRegistradoException("El correo electronico ya esta registrado");
        
    }

    Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
    .orElseThrow(() -> new RuntimeException("Rol Cliente no encontrado")); 
    
    Usuario usuario = Usuario.builder()
            .nombre(request.nombre())
            .apellido(request.apellido())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .rol(rolCliente)
            .fechaRegistro(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .build();

    usuarioRepository.save(usuario);

   }

   @Override
   public org.springframework.data.domain.Page<com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse> obtenerUsuarios(org.springframework.data.domain.Pageable pageable) {
       return usuarioRepository.findAll(pageable).map(usuario -> 
           com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse.builder()
               .id(usuario.getId())
               .nombre(usuario.getNombre())
               .apellido(usuario.getApellido())
               .username(usuario.getNombre() + " " + usuario.getApellido())
               .email(usuario.getEmail())
               .rol(usuario.getRol().getNombre())
               .build()
       );
   }

   @Override
   public com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse cambiarRol(Long id, com.example.venta_entrada.usuarios.dtos.request.CambiarRolRequest request) {
       Usuario usuario = usuarioRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
               
       Rol nuevoRol = rolRepository.findByNombre(request.getNuevoRol())
               .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
               
       usuario.setRol(nuevoRol);
       usuario.setFechaActualizacion(LocalDateTime.now());
       usuarioRepository.save(usuario);
       
       return com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse.builder()
               .id(usuario.getId())
               .nombre(usuario.getNombre())
               .apellido(usuario.getApellido())
               .username(usuario.getNombre() + " " + usuario.getApellido())
               .email(usuario.getEmail())
               .rol(usuario.getRol().getNombre())
               .build();
   }

   @Override
   public com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse editarUsuario(Long id, com.example.venta_entrada.usuarios.dtos.request.EditarUsuarioRequest request) {
       Usuario usuario = usuarioRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
               
       usuario.setNombre(request.getNombre());
       usuario.setApellido(request.getApellido());
       usuario.setEmail(request.getEmail());
       usuario.setFechaActualizacion(LocalDateTime.now());
       
       usuarioRepository.save(usuario);
       
       return com.example.venta_entrada.usuarios.dtos.response.UsuarioResponse.builder()
               .id(usuario.getId())
               .nombre(usuario.getNombre())
               .apellido(usuario.getApellido())
               .username(usuario.getNombre() + " " + usuario.getApellido())
               .email(usuario.getEmail())
               .rol(usuario.getRol().getNombre())
               .build();
   }

   @Override
   public void eliminarUsuario(Long id) {
       if (!usuarioRepository.existsById(id)) {
           throw new RuntimeException("Usuario no encontrado");
       }
       usuarioRepository.deleteById(id);
   }
}