package mx.edomex.rutas.clientes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRegistroRequest(

        @NotBlank @Size(max = 150)
        String nombreCompleto,

        @NotBlank @Email @Size(max = 150)
        String correoElectronico,

        @NotBlank @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
        String telefono,

        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,

        @Valid @NotNull
        DireccionRequest direccionInicial
) {}
