package mx.edomex.rutas.clientes.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nombreCompleto,
        String correoElectronico,
        String telefono,
        Boolean activo,
        List<DireccionResponse> direcciones,
        Instant createdAt
) {}
