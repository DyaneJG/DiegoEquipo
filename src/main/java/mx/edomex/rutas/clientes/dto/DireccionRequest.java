package mx.edomex.rutas.clientes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.edomex.rutas.clientes.model.ZonaEntrega;

public record DireccionRequest(

        @Size(max = 50)
        String alias,

        @NotBlank @Size(max = 150)
        String calle,

        @NotBlank @Size(max = 20)
        String numeroExterior,

        @Size(max = 20)
        String numeroInterior,

        @NotBlank @Size(max = 100)
        String colonia,

        @NotNull
        ZonaEntrega zona,

        @NotBlank @Pattern(regexp = "\\d{5}", message = "El código postal debe tener 5 dígitos")
        String codigoPostal,

        @Size(max = 300)
        String referencias,

        Boolean esPredeterminada
) {}
