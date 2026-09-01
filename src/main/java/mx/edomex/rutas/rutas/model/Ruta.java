package mx.edomex.rutas.rutas.model;

import jakarta.persistence.*;
import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.repartidores.model.Repartidor;

/**
 * Entidad 'Ruta'.
 * Actúa como una tabla intermedia que vincula
 * un Pedido específico con el Repartidor que lo va a entregar
 */
@Entity
@Table(name = "rutas", schema = "logistica")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Un pedido físico solo puede estar en una ruta de entrega a la vez
     * No podemos mandar a dos repartidores a entregar la misma hamburguesa/paquete
     */
    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /**
     * Un repartidor es un empleado constante. A lo largo del día,
     * este mismo repartidor estará asociado a "muchas" rutas diferentes
     */
    @ManyToOne
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;

    // Estado interno del viaje: "ASIGNADA", "EN_PROGRESO", "FINALIZADA"
    @Column(name = "estado")
    private String estado;

    // Getters y Setters
    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Repartidor getRepartidor() { return repartidor; }
    public String getEstado() { return estado; }

    public void setId(Long id) { this.id = id; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }
    public void setEstado(String estado) { this.estado = estado; }
}