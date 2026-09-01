package mx.edomex.rutas.tracking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import mx.edomex.rutas.repartidores.model.Repartidor;
import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla 'ubicaciones' en la base de datos PostgreSQL
 * Representa un punto individual en el mapa
 */
@Entity
@Table(name = "ubicaciones", schema = "logistica")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    // Se usa como marca de tiempo para saber exactamente a qué hora el repartidor
    // estuvo en esta coordenada
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /**
     * RELACIÓN: Muchos registros de ubicación pertenecen a Un repartidor.
     * FetchType.LAZY: Optimización de base de datos. Evita traer todos los datos del repartidor
     * (nombre, teléfono, etc.) cada vez que solo queremos ver una coordenada, ahorrando memoria.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)

    // @JsonIgnore, evita el error de referencia circular
    @JsonIgnore
    private Repartidor repartidor;

    public Ubicacion() {
        this.fechaHora = LocalDateTime.now(); // Por defecto asume el momento de creación
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Repartidor getRepartidor() { return repartidor; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }
}