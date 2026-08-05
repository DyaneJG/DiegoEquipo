package mx.edomex.rutas.repartidores.controller;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.service.RepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService repartidorService;

    public RepartidorController(RepartidorService repartidorService) {
        this.repartidorService = repartidorService;
    }

    @PostMapping
    public ResponseEntity<Repartidor> registrarRepartidor(@Valid @RequestBody Repartidor repartidor) {
        Repartidor nuevoRepartidor = repartidorService.registrarRepartidor(repartidor);
        return new ResponseEntity<>(nuevoRepartidor, HttpStatus.CREATED);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Repartidor>> listarRepartidores() {
        List<Repartidor> repartidores = repartidorService.obtenerRepartidoresDisponibles();
        return ResponseEntity.ok(repartidores);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repartidor> actualizarRepartidor(
            @PathVariable Long id,
            @Valid @RequestBody Repartidor repartidorActualizado) {

        Repartidor actualizado = repartidorService.actualizarRepartidor(id, repartidorActualizado);
        return ResponseEntity.ok(actualizado);
    }

    // Endpoint DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarRepartidor(@PathVariable Long id) {
        try {
            repartidorService.eliminarRepartidor(id);
            return ResponseEntity.ok("¡Éxito! El repartidor y todo su historial han sido eliminados del sistema.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}