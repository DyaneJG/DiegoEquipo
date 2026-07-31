package mx.edomex.rutas.clientes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edomex.rutas.infraestructura.model.BaseEntity;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "direcciones", schema = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "calle", nullable = false, length = 150)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 20)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @Column(name = "colonia", nullable = false, length = 100)
    private String colonia;

    @Enumerated(EnumType.STRING)
    @Column(name = "zona", nullable = false, length = 30)
    private ZonaEntrega zona;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(name = "referencias", length = 300)
    private String referencias;

    @Column(name = "ubicacion", columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(name = "validada", nullable = false)
    @Builder.Default
    private Boolean validada = false;

    @Column(name = "es_predeterminada", nullable = false)
    @Builder.Default
    private Boolean esPredeterminada = false;
}
