package mx.edomex.rutas.notificaciones.service;

import mx.edomex.rutas.notificaciones.DTO.CorreoRequestDTO;

public interface NotificacionService {
    
    void enviarCorreo(CorreoRequestDTO request);
}