package mx.edomex.rutas.pagos.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
// import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pedido_id", columnDefinition = "uuid", nullable = false)
    private UUID pedidoId;

    @Column(name = "metodo_pago_id", columnDefinition = "uuid", nullable = false)
    private UUID metodoPagoId;

    @Column(name = "proveedor", length = 30)
    // @Builder.Default
    private String proveedor;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia_externa", length = 200)
    private String referenciaExterna;

    @Column(name = "qr_codigo")
    private String qrCodigo;

    @Column(name = "qr_expira_at", columnDefinition = "timestamptz")
    private OffsetDateTime qrExpiraAt;

    @Column(name = "pagado_at", columnDefinition = "timestamptz")
    private OffsetDateTime pagadoAt;

    @CreationTimestamp
    @Column(name = "creado_en", columnDefinition = "timestamptz", updatable = false)
    private OffsetDateTime creadoEn;
}
