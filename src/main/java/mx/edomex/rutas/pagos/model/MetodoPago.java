package mx.edomex.rutas.pagos.model;

import jakarta.persistence.*;
import java.util.UUID;

/*
 * Métodos de pago registrados en la base de datos.
 */
@Entity
@Table(name = "metodos_pago")
public class MetodoPago {

    // Id del método
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Código del método (codi, stripe, etc.)
    @Column(nullable = false, unique = true)
    private String codigo;

    // Nombre del método
    @Column(nullable = false)
    private String nombre;

    // Indica si necesita token
    @Column(name = "requiere_token")
    private Boolean requiereToken;

    // Indica si está activo
    @Column(nullable = false)
    private Boolean activo;

    public MetodoPago() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getRequiereToken() {
        return requiereToken;
    }

    public void setRequiereToken(Boolean requiereToken) {
        this.requiereToken = requiereToken;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

}