package mx.edomex.rutas.rutas.model;

import jakarta.persistence.*;
import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.repartidores.model.Repartidor;

@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1 a 1: Una ruta pertenece a un solo pedido
    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Relación Muchos a 1: Un repartidor puede hacer muchas rutas (en distintos momentos)
    @ManyToOne
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;

    @Column(name = "estado")
    private String estado; // "ASIGNADA", "EN_PROGRESO", "FINALIZADA"

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Repartidor getRepartidor() {
        return repartidor;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public void setRepartidor(Repartidor repartidor) {
        this.repartidor = repartidor;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}