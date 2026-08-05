package mx.edomex.rutas.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.edomex.rutas.pedidos.model.EstadoPedido;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
  private UUID id;
  private String numeroPedido;
  private UUID clienteId;
  private UUID direccionEntregaId;
  private EstadoPedido estado;
  private BigDecimal subtotal;
  private BigDecimal iva;
  private BigDecimal cargoEntrega;
  private String notas;
  private LocalTime ventanaInicio;
  private LocalTime ventanaFin;
  private OffsetDateTime fechaEntregaProgramada;
  private OffsetDateTime fechaEntregaReal;
  private String canceladoMotivo;
  private OffsetDateTime creadoEn;
  private OffsetDateTime actualizadoEn;
}
