package com.example.venta_entrada.core.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.example.venta_entrada.usuarios.models.Usuario;
import com.example.venta_entrada.usuarios.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        
        return new User(
            usuario.getEmail(),
            usuario.getPassword(),
            List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
    }
}
