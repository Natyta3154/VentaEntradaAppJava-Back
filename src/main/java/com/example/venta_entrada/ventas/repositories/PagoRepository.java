package com.example.venta_entrada.ventas.repositories;

import com.example.venta_entrada.ventas.models.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByCompraId(Long compraId);
}
