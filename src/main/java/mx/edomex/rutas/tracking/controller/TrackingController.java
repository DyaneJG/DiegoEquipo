package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.dto.UbicacionRequestDTO;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.service.UbicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador principal para el rastreo de repartidores.
 * Maneja tanto conexiones persistentes (WebSockets) para alta frecuencia en tiempo real,
 * como peticiones HTTP tradicionales (REST) para pruebas o respaldos.
 */
@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * CANAL WEBSOCKET: Recepción de coordenadas en tiempo real.
     * Se usa WebSocket en lugar de HTTP para evitar la sobrecarga de abrir y cerrar
     * conexiones constantemente desde la app móvil del repartidor.
     */
    @MessageMapping("/tracking/{repartidorId}")
    public void actualizarUbicacionWebSocket(@DestinationVariable Long repartidorId, @Payload UbicacionRequestDTO peticion) {

        System.out.println("[Repartidor " + repartidorId + "] Nueva ubicación recibida vía WebSocket: Lat " + peticion.getLatitud() + ", Lon " + peticion.getLongitud());

        try {
            // 1. Vinculamos la coordenada al repartidor correspondiente
            peticion.setRepartidorId(repartidorId);

            // 2. Persistencia: Guardamos el historial en la base de datos para futuras auditorías o trazado de rutas
            Ubicacion nuevaUbicacion = new Ubicacion();
            nuevaUbicacion.setLatitud(peticion.getLatitud());
            nuevaUbicacion.setLongitud(peticion.getLongitud());
            ubicacionService.registrarUbicacion(repartidorId, nuevaUbicacion);

            // 3. Distribución (Broadcast): Retransmitimos la coordenada a todos los clientes (ej. dashboard web)
            // que estén suscritos al canal específico de este repartidor.
            String canalRastreo = "/topic/repartidor/" + repartidorId;
            messagingTemplate.convertAndSend(canalRastreo, peticion);

        } catch (RuntimeException e) {
            // Evitamos que un error de base de datos tire la conexión WebSocket completa
            System.err.println(" Error al procesar ubicación del repartidor " + repartidorId + ": " + e.getMessage());
        }
    }

    /**
     * ENDPOINTS HTTP REST
     * Sirven como mecanismo de contingencia (fallback) si fallan los WebSockets
     * o para simular envíos de coordenadas desde herramientas como Postman.
     */

    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizarUbicacion(@RequestBody UbicacionRequestDTO peticion) {
        try {
            // Persistencia del dato
            Ubicacion nuevaUbicacion = new Ubicacion();
            nuevaUbicacion.setLatitud(peticion.getLatitud());
            nuevaUbicacion.setLongitud(peticion.getLongitud());
            ubicacionService.registrarUbicacion(peticion.getRepartidorId(), nuevaUbicacion);

            // Retransmisión al dashboard, replicando el comportamiento del WebSocket
            String canalRastreo = "/topic/repartidor/" + peticion.getRepartidorId();
            messagingTemplate.convertAndSend(canalRastreo, peticion);

            return ResponseEntity.ok("Coordenadas actualizadas correctamente para el repartidor " + peticion.getRepartidorId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Permite reiniciar la ruta de un repartidor (por ejemplo, al iniciar un nuevo turno)
     * borrando sus coordenadas históricas.
     */
    @DeleteMapping("/limpiar/{repartidorId}")
    public ResponseEntity<String> limpiarUbicaciones(@PathVariable Long repartidorId) {
        try {
            ubicacionService.limpiarHistorialDelRepartidor(repartidorId);
            return ResponseEntity.ok("Historial borrado exitosamente para el repartidor ID: " + repartidorId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}