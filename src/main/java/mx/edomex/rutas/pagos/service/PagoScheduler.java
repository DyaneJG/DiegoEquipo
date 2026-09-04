package mx.edomex.rutas.pagos.service;

import mx.edomex.rutas.pagos.model.EstadoPago;
import mx.edomex.rutas.pagos.repository.PagoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        var pagos = pagoRepository.findByEstado(
                EstadoPago.PENDIENTE);

        for (var pago : pagos) {

            if (pago.getQrExpiraAt() != null &&
                    pago.getQrExpiraAt().isBefore(LocalDateTime.now())) {

                pago.setEstado(EstadoPago.CANCELADO);

                pagoRepository.save(pago);

            }

        }

    }

}