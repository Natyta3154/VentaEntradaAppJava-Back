package com.example.venta_entrada.usuarios.repositories;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.venta_entrada.usuarios.models.Rol;

/**
 * Repositorio para la entidad {@link Rol}.
 * Proporciona métodos para interactuar con la tabla de roles en la base de datos.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long>{

    /**
     * Busca un rol en la base de datos por su nombre.
     * Útil para buscar roles por defecto (como "ADMIN" o "USER") y asignarlos a usuarios nuevos.
     *
     * @param nombre el nombre del rol a buscar.
     * @return un {@link Optional} que contiene el Rol si se encuentra, o vacío si no existe.
     */
    Optional<Rol> findByNombre(String nombre);
    
}



