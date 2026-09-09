package com.example.venta_entrada.ventas.repositories;

import com.example.venta_entrada.ventas.models.TipoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoEntradaRepository extends JpaRepository<TipoEntrada, Long> {
    List<TipoEntrada> findByEventoId(Long eventoId);
}
