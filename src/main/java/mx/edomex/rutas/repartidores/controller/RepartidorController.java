package mx.edomex.rutas.repartidores.controller;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.service.RepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

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
        // Ahora sí usamos el nombre exacto de tu método
        Repartidor nuevoRepartidor = repartidorService.registrarRepartidor(repartidor);
        return new ResponseEntity<>(nuevoRepartidor, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Repartidor>> listarRepartidores() {
        // Ahora sí usamos el nombre exacto de tu método
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

}