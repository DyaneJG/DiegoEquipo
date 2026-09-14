package mx.edomex.rutas.clientes.dto;

import mx.edomex.rutas.clientes.model.ZonaEntrega;

import java.util.UUID;

public record DireccionResponse(
        UUID id,
        String alias,
        String calle,
        String numeroExterior,
        String numeroInterior,
        String colonia,
        ZonaEntrega zona,
        String codigoPostal,
        String referencias,
        Double latitud,
        Double longitud,
        Boolean validada,
        Boolean esPredeterminada
) {}
