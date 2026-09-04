package mx.edomex.rutas.pagos.dto;

import java.math.BigDecimal;
import java.util.UUID;

/*
 * Datos necesarios para crear un pago.
 */
public class CrearPagoRequest {

    // Pedido a pagar
    private UUID pedidoId;

    // Monto total
    private BigDecimal monto;

    public CrearPagoRequest() {
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}