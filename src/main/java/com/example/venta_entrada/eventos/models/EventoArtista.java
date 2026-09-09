package com.example.venta_entrada.eventos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

class EventoArtistaId implements Serializable {
    private Long eventoId;
    private Long artistaId;

    public EventoArtistaId() {}
    public EventoArtistaId(Long eventoId, Long artistaId) {
        this.eventoId = eventoId;
        this.artistaId = artistaId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventoArtistaId that = (EventoArtistaId) o;
        return Objects.equals(eventoId, that.eventoId) &&
               Objects.equals(artistaId, that.artistaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventoId, artistaId);
    }
}

@Entity
@Table(name = "evento_artista")
@IdClass(EventoArtistaId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoArtista {

    @Id
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;
    
    @Id
    @Column(name = "artista_id", nullable = false)
    private Long artistaId;

}
