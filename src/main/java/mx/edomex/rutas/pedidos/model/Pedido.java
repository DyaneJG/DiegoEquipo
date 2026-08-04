package mx.edomex.rutas.pedidos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estado")
    private String estado; // Aquí buscaremos los que digan "PAGADO"

    @Column(name = "direccion_destino")
    private String direccionDestino;

    public Long getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

}