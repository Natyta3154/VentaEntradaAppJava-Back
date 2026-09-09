package com.example.venta_entrada.ventas.repositories;

import com.example.venta_entrada.ventas.models.Entrada;
import com.example.venta_entrada.ventas.models.EstadoEntrada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    Optional<Entrada> findByCodigoQr(String codigoQr);
    List<Entrada> findByCompraId(Long compraId);
    Page<Entrada> findByCompraUsuarioId(Long usuarioId, Pageable pageable);
    int countByEventoIdAndEstadoNot(Long eventoId, EstadoEntrada estado);
    int countByTipoEntradaIdAndEstadoNot(Long tipoEntradaId, EstadoEntrada estado);
}
