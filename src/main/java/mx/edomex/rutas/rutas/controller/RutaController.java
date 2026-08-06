package mx.edomex.rutas.rutas.controller;

import mx.edomex.rutas.rutas.model.Ruta;
import mx.edomex.rutas.rutas.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rutas") // URL base
public class RutaController {

    @Autowired
    private RutaService rutaService;

    // Escuchará peticiones POST en /api/rutas/asignar/1001
    @PostMapping("/asignar/{pedidoId}")
    public ResponseEntity<String> asignarRuta(@PathVariable Long pedidoId) {
        try {
            Ruta nuevaRuta = rutaService.asignarRepartidorAPedido(pedidoId);

            return ResponseEntity.ok("¡Éxito! Ruta asignada. El repartidor "
                    + nuevaRuta.getRepartidor().getNombreCompleto()
                    + " va en camino.");

        } catch (RuntimeException e) {
            // Si no hay repartidores o el pedido no está PAGADO, mandamos el error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Escuchará peticiones POST en /api/rutas/completar/1001
    @PostMapping("/completar/{pedidoId}")
    public ResponseEntity<String> completarEntrega(@PathVariable Long pedidoId) {
        try {
            // Llamamos a la lógica para cerrar la entrega
            String mensaje = rutaService.finalizarEntrega(pedidoId);
            return ResponseEntity.ok(mensaje);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}