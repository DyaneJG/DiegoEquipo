package mx.edomex.rutas.pagos.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
	private UUID pedidoId;
	private String metodoPagoCodigo;
	private BigDecimal monto;
	private String moneda = "mxn";
	private String descripcion;

	
}
