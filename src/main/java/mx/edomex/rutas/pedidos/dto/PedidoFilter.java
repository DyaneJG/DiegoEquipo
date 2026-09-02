package mx.edomex.rutas.pedidos.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;
import mx.edomex.rutas.pedidos.model.EstadoPedido;

@Data
public class PedidoFilter {
  private UUID clienteId;
  private EstadoPedido estado;
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private String numeroPedido;
}
