package mx.edomex.rutas.clientes.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteActualizacionRequest(

        @Size(max = 150)
        String nombreCompleto,

        @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
        String telefono
) {}
