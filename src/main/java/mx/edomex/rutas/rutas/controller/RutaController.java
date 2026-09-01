package mx.edomex.rutas.rutas.controller;

import java.util.UUID;
import mx.edomex.rutas.rutas.model.Ruta;
import mx.edomex.rutas.rutas.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Se encarga de recibir las peticiones para iniciar o terminar entregas.
 */
@RestController
@RequestMapping("/api/rutas") // Endpoint base unificado
public class RutaController {

    @Autowired
    private RutaService rutaService;

    /**
     * Endpoint para vincular un pedido listo con un repartidor disponible.
     * Usamos UUID para el pedido
     */
    @PostMapping("/asignar/{pedidoId}")
    public ResponseEntity<String> asignarRuta(@PathVariable UUID pedidoId) {
        try {
            // Delegamos la lógica pesada al servicio
            Ruta nuevaRuta = rutaService.asignarRepartidorAPedido(pedidoId);

            return ResponseEntity.ok("¡Éxito! Ruta asignada. El repartidor "
                    + nuevaRuta.getRepartidor().getNombreCompleto()
                    + " va en camino.");

        } catch (RuntimeException e) {
            // Manejo de escenarios de negocio: sin repartidores, pedido cancelado, etc.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint que detona el repartidor desde su app cuando entrega el paquete
     * * Libera al repartidor y da por concluido el proceso
     */
    @PostMapping("/completar/{pedidoId}")
    public ResponseEntity<String> completarEntrega(@PathVariable UUID pedidoId) {
        try {
            String mensaje = rutaService.finalizarEntrega(pedidoId);
            return ResponseEntity.ok(mensaje);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}