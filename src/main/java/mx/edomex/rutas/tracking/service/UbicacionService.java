package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.tracking.model.Ubicacion;
import java.util.List;

/**
 * Contrato del servicio. Define QUÉ operaciones se pueden hacer con las ubicaciones
 * Usar interfaces nos permite despues poder cambiar la logica interna sin romper los controladores
 */
public interface UbicacionService {
    Ubicacion registrarUbicacion(Long repartidorId, Ubicacion ubicacion);
    List<Ubicacion> obtenerHistorialPorRepartidor(Long repartidorId);
    void limpiarHistorialDelRepartidor(Long repartidorId);
}