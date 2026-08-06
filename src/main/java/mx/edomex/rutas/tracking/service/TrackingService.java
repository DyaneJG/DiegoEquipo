package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.repository.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TrackingService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Transactional
    public void registrarUbicacion(Long repartidorId, Double latitud, Double longitud) {

        // 1. Buscamos al repartidor que está mandando su GPS
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado"));

        // 2. Creamos un nuevo registro de ubicación
        Ubicacion nuevaUbicacion = new Ubicacion();
        nuevaUbicacion.setRepartidor(repartidor);
        nuevaUbicacion.setLatitud(latitud);
        nuevaUbicacion.setLongitud(longitud);

        // Usamos la hora exacta del servidor en este momento
        nuevaUbicacion.setFechaHora(LocalDateTime.now());

        // 3. Guardamos en la base de datos
        ubicacionRepository.save(nuevaUbicacion);
    }
}