package mx.edomex.rutas.repartidores.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import java.util.List;

public interface RepartidorService {
    Repartidor registrarRepartidor(Repartidor repartidor);
    List<Repartidor> obtenerRepartidoresDisponibles();
    Repartidor actualizarRepartidor(Long id, Repartidor detallesRepartidor);
    void eliminarRepartidor(Long id);
    Repartidor obtenerRepartidorPorId(Long id);
}