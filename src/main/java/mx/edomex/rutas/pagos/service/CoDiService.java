package mx.edomex.rutas.pagos.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/*
 * Genera la información del pago CoDi.
 */
@Service
public class CoDiService implements ProveedorPago {

    @Override
    public String generarReferencia() {

        return UUID.randomUUID().toString();

    }

    @Override
    public String generarContenidoQR(
            String referencia,
            BigDecimal monto
    ) {

        return "CODI"
                + "|REF=" + referencia
                + "|MONTO=" + monto;

    }

    @Override
    public boolean confirmarPago(String referencia) {

        /*
         * Aquí después se consultará al banco.
         */

        return true;

    }

}