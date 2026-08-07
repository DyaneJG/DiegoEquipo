package mx.edomex.rutas.repartidores.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.edomex.rutas.tracking.model.Ubicacion;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repartidores", schema = "logistica")
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio y no puede estar vacío")
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 10, max = 10, message = "El teléfono debe tener exactamente 10 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El teléfono solo puede contener números")
    private String telefono;

    @NotBlank(message = "La placa del vehículo es obligatoria")
    private String placaVehiculo;

    @NotBlank(message = "El estado no puede estar vacío")
    private String estado;

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    // Un repartidor tiene muchas ubicaciones.
    // cascade = CascadeType.ALL, si se borra al repartidor se borra su historial
    @JsonIgnore
    @OneToMany(mappedBy = "repartidor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ubicacion> historialUbicaciones = new ArrayList<>();

    public Repartidor() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public List<Ubicacion> getHistorialUbicaciones() {
        return historialUbicaciones;
    }

    public void setHistorialUbicaciones(List<Ubicacion> historialUbicaciones) {
        this.historialUbicaciones = historialUbicaciones;
    }
}