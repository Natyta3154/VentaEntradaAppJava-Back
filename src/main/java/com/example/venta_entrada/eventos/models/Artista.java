package com.example.venta_entrada.eventos.models;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SQLDelete(sql = "UPDATE artistas SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Entity
@Table(name = "artistas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "estilo", nullable = false, length = 50)
    private String estiloMusical;
    
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "foto")
    private String foto;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;



    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;
}
