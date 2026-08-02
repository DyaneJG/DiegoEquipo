package mx.edomex.rutas.pagos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Guarda la información de un pago.
 */
@Entity
@Table(name = "pagos")
public class Pago {

    // Id del pago
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Id del pedido
    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    // Método de pago
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    // Proveedor del pago
    private String proveedor;

    // Estado del pago
    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    // Monto del pago
    private BigDecimal monto;

    // Referencia generada por CoDi
    @Column(name = "referencia_externa")
    private String referenciaExterna;

    // Información que contendrá el QR
    @Column(name = "qr_codigo", columnDefinition = "TEXT")
    private String qrCodigo;

    // Fecha de expiración del QR
    @Column(name = "qr_expira_at")
    private LocalDateTime qrExpiraAt;

    // Fecha en que se pagó
    @Column(name = "pagado_at")
    private LocalDateTime pagadoAt;

    // Fecha de creación
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    public Pago() {
        this.creadoEn = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public String getQrCodigo() {
        return qrCodigo;
    }

    public void setQrCodigo(String qrCodigo) {
        this.qrCodigo = qrCodigo;
    }

    public LocalDateTime getQrExpiraAt() {
        return qrExpiraAt;
    }

    public void setQrExpiraAt(LocalDateTime qrExpiraAt) {
        this.qrExpiraAt = qrExpiraAt;
    }

    public LocalDateTime getPagadoAt() {
        return pagadoAt;
    }

    public void setPagadoAt(LocalDateTime pagadoAt) {
        this.pagadoAt = pagadoAt;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}