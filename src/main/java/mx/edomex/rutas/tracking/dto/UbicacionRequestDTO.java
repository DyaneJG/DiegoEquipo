package mx.edomex.rutas.tracking.dto;

/**
 * Patrón DTO (Data Transfer Object).
 * ¿Por qué?
 * 1. Seguridad: Evitamos exponer la estructura interna de la base de datos.
 * 2. Rendimiento: Recibimos de Android SOLO los datos que necesitamos osea las coordenadas,
 * sin obligar a la app móvil a enviar campos de auditoría como la fecha o relaciones complejas
 */
public class UbicacionRequestDTO {

    private Long repartidorId;
    private Double latitud;
    private Double longitud;

    // Getters y Setters
    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}