package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.service.UbicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador REST para el manejo del historial de ubicaciones.
 * A diferencia del TrackingController (que maneja el tiempo real vía WebSockets),
 * este controlador se encarga de las consultas de datos persistidos en la base de datos,
 * ideal para dibujar rutas pasadas o hacer auditorías.
 */
@RestController
@RequestMapping("/api/repartidores")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    // Inyección de dependencias por constructor (recomendado por Spring sobre @Autowired)
    // Facilita la creación de pruebas unitarias (Mocks).
    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @PostMapping("/{id}/ubicaciones")
    public ResponseEntity<Ubicacion> registrarUbicacion(
            @PathVariable Long id,
            @RequestBody Ubicacion ubicacion) {

        // Delegamos toda la lógica de validación al servicio. El controlador solo debe
        // encargarse de recibir peticiones HTTP y devolver respuestas HTTP
        Ubicacion nuevaUbicacion = ubicacionService.registrarUbicacion(id, ubicacion);
        return new ResponseEntity<>(nuevaUbicacion, HttpStatus.CREATED);
    }

    /**
     * Endpoint para consultar toda la ruta trazada por un repartidor.
     * Útil para el panel de administración cuando se quiere ver por dónde pasó.
     */
    @GetMapping("/{id}/ubicaciones")
    public ResponseEntity<List<Ubicacion>> obtenerHistorialUbicaciones(@PathVariable Long id) {
        List<Ubicacion> historial = ubicacionService.obtenerHistorialPorRepartidor(id);
        return ResponseEntity.ok(historial);
    }
}