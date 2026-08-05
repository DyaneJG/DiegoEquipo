package mx.edomex.rutas.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {
  @NotNull(message = "El ID del cliente es requerido")
  private UUID clienteId;

  @NotNull(message = "La direccion de entrega es requerida")
  private UUID direccionEntregaId;

  @NotNull(message = "El subtotal es requerido")
  @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
  @DecimalMax(value = "999999.99", message = "El subtotal no puede ser mayor a 999999.99")
  private BigDecimal subtotal;

  @NotNull(message = "El IVA es requerido")
  @DecimalMin(value = "0.1", message = "El IVA debe ser mayor a 0")
  @DecimalMax(value = "999999.99", message = "El IVA no puede ser mayor a 999999.99")
  private BigDecimal iva;

  @DecimalMin(value = "0.00", message = "El cargo no puede ser negativo")
  private BigDecimal cargoEntrega;
  
  private String notas;
  private LocalTime ventanaInicio;
  private LocalTime ventanaFin;
}
