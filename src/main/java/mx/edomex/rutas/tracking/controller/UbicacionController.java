package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.service.UbicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/repartidores")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @PostMapping("/{id}/ubicaciones")
    public ResponseEntity<Ubicacion> registrarUbicacion(
            @PathVariable Long id,
            @RequestBody Ubicacion ubicacion) {

        Ubicacion nuevaUbicacion = ubicacionService.registrarUbicacion(id, ubicacion);
        return new ResponseEntity<>(nuevaUbicacion, HttpStatus.CREATED);
    }
    // Ruta para leer el historial GET
    @GetMapping("/{id}/ubicaciones")
    public ResponseEntity<List<Ubicacion>> obtenerHistorialUbicaciones(@PathVariable Long id) {

        List<Ubicacion> historial = ubicacionService.obtenerHistorialPorRepartidor(id);
        return ResponseEntity.ok(historial);
    }
}