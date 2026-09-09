package com.example.venta_entrada.eventos.models;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SQLDelete(sql = "UPDATE evento_imagenes SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Entity
@Table(name = "evento_imagenes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre_imagen", nullable = false, length = 55)
    private String nombreImagen;

    @Column(name = "url_imagen", nullable = false, length = 255)
    private String urlImagen;
    
    @Column(name = "orden", nullable = false)
    private int orden;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;


    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;
}
