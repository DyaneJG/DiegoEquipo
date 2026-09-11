package mx.edomex.rutas.notificaciones.controller;

import mx.edomex.rutas.notificaciones.DTO.CorreoRequestDTO;
import mx.edomex.rutas.notificaciones.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping("/correo")
    public ResponseEntity<String> enviarCorreo(
            @RequestBody CorreoRequestDTO request) {

        notificacionService.enviarCorreo(request);

        return ResponseEntity.ok("Correo procesado correctamente");
    }
}