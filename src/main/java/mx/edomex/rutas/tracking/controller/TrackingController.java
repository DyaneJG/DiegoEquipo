package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.dto.UbicacionRequestDTO;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.service.UbicacionService;
import mx.edomex.rutas.repartidores.service.GeocercaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GeocercaService geocercaService;
    // TODO (TEMPORAL): Reemplazar LATITUD_DESTINO_MOCK por la coordenada real de
    //  la BD con la tabla DireccionesCliente.
    private static final double LATITUD_DESTINO_MOCK = 19.5442857;
    private static final double LONGITUD_DESTINO_MOCK = -98.8795375;

    //Zona repartidor

    @MessageMapping("/tracking/{repartidorId}")
    public void actualizarUbicacionWebSocket(@DestinationVariable Long repartidorId, @Payload UbicacionRequestDTO peticion) {
        System.out.println("[Repartidor " + repartidorId + "] GPS recibido: Lat " + peticion.getLatitud() + ", Lon " + peticion.getLongitud());
        try {
            peticion.setRepartidorId(repartidorId);

            Ubicacion nuevaUbicacion = new Ubicacion();
            nuevaUbicacion.setLatitud(peticion.getLatitud());
            nuevaUbicacion.setLongitud(peticion.getLongitud());
            ubicacionService.registrarUbicacion(repartidorId, nuevaUbicacion);

            String canalAdmin = "/topic/repartidor/" + repartidorId;
            messagingTemplate.convertAndSend(canalAdmin, peticion);

            double distancia = geocercaService.calcularDistanciaEnMetros(
                    peticion.getLatitud(), peticion.getLongitud(),
                    LATITUD_DESTINO_MOCK, LONGITUD_DESTINO_MOCK
            );

            String estadoPedido = geocercaService.determinarEstadoPorDistancia(distancia);
            System.out.println("   -> Distancia al destino: " + String.format("%.2f", distancia) + " metros. Estado actual: " + estadoPedido);

            messagingTemplate.convertAndSend("/topic/pedido/100/estado", "{\"estado\": \"" + estadoPedido + "\"}");
        } catch (RuntimeException e) {
            System.err.println(" Error al procesar ubicación del repartidor " + repartidorId + ": " + e.getMessage());
        }
    }

    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizarUbicacion(@RequestBody UbicacionRequestDTO peticion) {
        try {
            Ubicacion nuevaUbicacion = new Ubicacion();
            nuevaUbicacion.setLatitud(peticion.getLatitud());
            nuevaUbicacion.setLongitud(peticion.getLongitud());
            ubicacionService.registrarUbicacion(peticion.getRepartidorId(), nuevaUbicacion);

            String canalRastreo = "/topic/repartidor/" + peticion.getRepartidorId();
            messagingTemplate.convertAndSend(canalRastreo, peticion);

            return ResponseEntity.ok("Coordenadas actualizadas correctamente para el repartidor " + peticion.getRepartidorId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Zona Admin

    /**
     * Endpoint protegido. Solo un usuario con el rol de ADMIN puede borrar el historial
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/limpiar/{repartidorId}")
    public ResponseEntity<String> limpiarUbicaciones(@PathVariable Long repartidorId) {
        try {
            ubicacionService.limpiarHistorialDelRepartidor(repartidorId);
            return ResponseEntity.ok("Historial borrado exitosamente para el repartidor ID: " + repartidorId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}