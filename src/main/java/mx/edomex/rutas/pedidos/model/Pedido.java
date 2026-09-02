package mx.edomex.rutas.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos", schema = "test")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
  
  @Id
  @GeneratedValue( strategy = GenerationType.UUID)
  @Column( name ="id", nullable = false, updatable = false)
  private UUID id;
  
  @Column( name = "numero_pedido", length = 50, nullable = false, unique = true)
  private String numeroPedido;

  @Column(name = "cliente_id", columnDefinition = "uuid", nullable = false)
    private UUID clienteId;

    @Column(name = "direccion_entrega_id", columnDefinition = "uuid", nullable = false)
    private UUID direccionEntregaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "iva", precision = 12, scale = 2, nullable = false)
    private BigDecimal iva;

    @Column(name = "cargo_entrega", precision = 8, scale = 2)
    private BigDecimal cargoEntrega;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "notas", columnDefinition = "text")
    private String notas;

    @Column(name = "ventana_inicio")
    private LocalTime ventanaInicio;

    @Column(name = "ventana_fin")
    private LocalTime ventanaFin;

    @Column(name = "fecha_entrega_programada")
    private LocalDate fechaEntregaProgramada;

    @Column(name = "fecha_entrega_real", columnDefinition = "timestamptz")
    private OffsetDateTime fechaEntregaReal;

    @Column(name = "cancelado_motivo", columnDefinition = "text")
    private String canceladoMotivo;

    @CreationTimestamp
    @Column(name = "creado_en", columnDefinition = "timestamptz", updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", columnDefinition = "timestamptz")
    private OffsetDateTime actualizadoEn;
}
