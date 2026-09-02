package mx.edomex.rutas.repartidores.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import java.util.List;

// interfaz pública
public interface RepartidorService {
    Repartidor registrarRepartidor(Repartidor repartidor);
    List<Repartidor> obtenerRepartidoresDisponibles();
    Repartidor actualizarRepartidor(Long id, Repartidor detallesRepartidor);
}