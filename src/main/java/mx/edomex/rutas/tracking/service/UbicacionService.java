package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.tracking.model.Ubicacion;

import java.util.List;

public interface UbicacionService {
    Ubicacion registrarUbicacion(Long repartidorId, Ubicacion ubicacion);
    List<Ubicacion> obtenerHistorialPorRepartidor(Long repartidorId);
}

