package mx.edomex.rutas.pagos.service;

import mx.edomex.rutas.pagos.dto.CrearPagoRequest;
import mx.edomex.rutas.pagos.dto.PagoResponse;
import org.springframework.stereotype.Service;

@Service
public class PagoService {

    /**
     * Este servicio quedó en estado temporal y debe ser reescrito en una
     * siguiente tarea de pagos, porque el bloque previo contenía código
     * corrupto y no compilable.
     *
     * Se mantiene sin lógica funcional para no afectar a los demás módulos,
     * ya que el objetivo actual es validar la arquitectura y el dashboard.
     */
    public PagoResponse crearPagoCoDi(CrearPagoRequest request) {
        return new PagoResponse();
    }

    public void confirmarPago(String referencia) {
        // pendiente de implementación real
    }
}
