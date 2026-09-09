package com.example.venta_entrada.usuarios.repositories;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.venta_entrada.usuarios.models.Usuario;


/**
 * Repositorio para la entidad {@link Usuario}.
 * Proporciona todas las operaciones CRUD básicas gracias a JpaRepository
 * y define métodos personalizados para consultas específicas.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
   
  /**
   * Busca un usuario en la base de datos por su correo electrónico.
   *
   * @param email el correo electrónico exacto del usuario a buscar.
   * @return un {@link Optional} que contiene el Usuario si se encuentra, o vacío si no existe.
   */
  @EntityGraph(attributePaths = {"rol"})
  Optional<Usuario> findByEmail(String email);

  /**
   * Verifica si ya existe un usuario registrado con el correo electrónico dado.
   * Muy útil para validaciones antes de registrar un usuario nuevo.
   *
   * @param email el correo electrónico a verificar.
   * @return true si el email ya está en uso, false en caso contrario.
   */
  boolean existsByEmail(String email);
}

