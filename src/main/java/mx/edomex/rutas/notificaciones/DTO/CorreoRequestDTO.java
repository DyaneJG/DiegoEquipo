package mx.edomex.rutas.notificaciones.DTO;
import java.util.UUID;

public record CorreoRequestDTO(
    UUID usuarioId,
    UUID pedidoId, 
    String correoDestino,
    String titulo,
    String cuerpo
) {}