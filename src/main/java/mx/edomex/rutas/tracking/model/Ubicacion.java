package mx.edomex.rutas.tracking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import mx.edomex.rutas.repartidores.model.Repartidor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ubicaciones")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    // Hora y fecha exacta en la que se guardo la coordenada
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    //fetch = FetchType. LAZY, solo traera longitud, fecha y latitud de las ubicaciones
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    @JsonIgnore // Evita que se haga un ciclo infinito al mostrar el JSON
    private Repartidor repartidor;

    public Ubicacion() {
        this.fechaHora = LocalDateTime.now(); // Por defecto toma la hora actual
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