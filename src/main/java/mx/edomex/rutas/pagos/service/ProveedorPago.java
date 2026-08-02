package mx.edomex.rutas.pagos.service;

import java.math.BigDecimal;

public interface ProveedorPago {

    String generarReferencia();

    String generarContenidoQR(
            String referencia,
            BigDecimal monto
    );

    boolean confirmarPago(String referencia);

}