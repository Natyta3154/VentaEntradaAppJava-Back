package com.example.venta_entrada.contacto.repositories;

import com.example.venta_entrada.contacto.models.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoRepository extends JpaRepository<MensajeContacto, Long> {
    List<MensajeContacto> findAllByOrderByFechaEnvioDesc();
}
