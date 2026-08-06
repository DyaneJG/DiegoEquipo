package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.dto.UbicacionRequestDTO;
import mx.edomex.rutas.tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    // Escuchará peticiones POST
    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizarUbicacion(@RequestBody UbicacionRequestDTO peticion) {
        try {
            trackingService.registrarUbicacion(
                    peticion.getRepartidorId(),
                    peticion.getLatitud(),
                    peticion.getLongitud()
            );
            return ResponseEntity.ok("Coordenadas actualizadas correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}