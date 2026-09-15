package mx.edomex.rutas.pagos.controller;

import mx.edomex.rutas.pagos.dto.CrearPagoRequest;
import mx.edomex.rutas.pagos.dto.PagoResponse;
import mx.edomex.rutas.pagos.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    // Servicio de pagos
    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /*
     * Genera un pago CoDi.
     */
    @PostMapping("/codi")
    public ResponseEntity<PagoResponse> crearPago(
            @RequestBody CrearPagoRequest request) {

        return ResponseEntity.ok(
                pagoService.crearPagoCoDi(request)
        );

    }

    /*
     * Confirma el pago.
     */
    @PostMapping("/confirmar/{referencia}")
    public ResponseEntity<String> confirmarPago(
            @PathVariable String referencia) {

        pagoService.confirmarPago(referencia);

        return ResponseEntity.ok("Pago confirmado");

    }

}