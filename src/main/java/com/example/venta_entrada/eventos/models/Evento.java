package com.example.venta_entrada.eventos.models;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SQLDelete(sql = "UPDATE eventos SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Entity
@Table(name = "eventos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "fecha_evento" , nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "ubicacion" , nullable = false , length = 200)
    private String ubicacion;
    
    @Column(name = "capacidad_total", nullable = false)
    private int capacidadTotal;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "edad_minima", length = 20)
    private int edadMinima;

    @Column(name = "imagen_portada", length = 1000)
    private String imagenPortada;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "evento", fetch = jakarta.persistence.FetchType.EAGER)
    private java.util.List<com.example.venta_entrada.ventas.models.TipoEntrada> tiposEntrada;

 @Column (name = "fecha_creacion", nullable = false , updatable = false)
 @CreationTimestamp
 private LocalDateTime fechaCreacion;

 @Column (name = "fecha_actualizacion", nullable = false)
 @UpdateTimestamp
 private LocalDateTime fechaActualizacion;


    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;
}
