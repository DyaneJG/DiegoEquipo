package mx.edomex.rutas.pagos.service;

import mx.edomex.rutas.pagos.model.EstadoPago;
import mx.edomex.rutas.pagos.repository.PagoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// FIX: Cambiamos a OffsetDateTime
import java.time.OffsetDateTime;

@Service
public class PagoScheduler {

    private final PagoRepository pagoRepository;

    public PagoScheduler(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    /*
     * Revisa pagos pendientes cada minuto.
     */
    @Scheduled(fixedRate = 60000)
    public void cancelarPagosExpirados() {

        var pagos = pagoRepository.findByEstado(EstadoPago.PENDIENTE.name());

        for (var pago : pagos) {

            if (pago.getQrExpiraAt() != null &&
                    pago.getQrExpiraAt().isBefore(OffsetDateTime.now())) {

                pago.setEstado(EstadoPago.CANCELADO.name());

                pagoRepository.save(pago);
            }
        }
    }
}