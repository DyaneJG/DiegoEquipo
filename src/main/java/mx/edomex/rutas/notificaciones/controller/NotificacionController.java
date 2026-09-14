package mx.edomex.rutas.notificaciones.controller;

import mx.edomex.rutas.notificaciones.DTO.CorreoRequestDTO;
import mx.edomex.rutas.notificaciones.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    // se inyecta el servicio
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }
    //Escucha peticiones HTTP para mandar correo
    @GetMapping("/prueba-correo")
    public ResponseEntity<String> probarEnvioCorreo(@RequestParam String destino) {
        
        // Se construye un paquete
        CorreoRequestDTO request = new CorreoRequestDTO(
            UUID.fromString("04a43f79-dbbc-4556-854e-1f91a23be14b"), // <--- AQUÍ PEGAS TU UUID ENTRE COMILLAS
            null,              
            destino,
            "Prueba correo",
            "se mando correctamente el correo."
        );

        // se envia el correo con el objeto CorreoRequestDTO
        notificacionService.enviarCorreo(request);

        // mensaje de exito
        return ResponseEntity.ok("Proceso terminado. Revisa el correo de: " + destino + " y tu tabla de 'notificaciones' en la BD.");
    }
}