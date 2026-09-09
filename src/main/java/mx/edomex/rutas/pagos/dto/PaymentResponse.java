package mx.edomex.rutas.pagos.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
  private String clientSecret;
  private String paymentIntentId;
  private String estado;
  private String qrCode;
  private Long expiraEn;
  private String moneda;
  private BigDecimal monto;
  private String mensaje;
  private String metodoPago;
}