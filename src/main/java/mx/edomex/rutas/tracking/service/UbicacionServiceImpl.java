package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import mx.edomex.rutas.tracking.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de las reglas de negocio para el rastreo
 * Aquí es donde esta la inteligencia de la aplicación antes de tocar la base de datos.
 */
@Service
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final RepartidorRepository repartidorRepository;

    public UbicacionServiceImpl(UbicacionRepository ubicacionRepository, RepartidorRepository repartidorRepository) {
        this.ubicacionRepository = ubicacionRepository;
        this.repartidorRepository = repartidorRepository;
    }

    @Override
    public Ubicacion registrarUbicacion(Long repartidorId, Ubicacion ubicacion) {
        // RN 1: Validar integridad. No podemos guardar la coordenada de un fantasma
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + repartidorId));

        ubicacion.setRepartidor(repartidor);

        // RN 2: Confiabilidad del tiempo.
        // Sobreescribimos la fecha con la del servidor para evitar que el celular
        // del repartidor envíe una hora falsa si la tiene mal configurada
        ubicacion.setFechaHora(LocalDateTime.now());

        return ubicacionRepository.save(ubicacion);
    }

    @Override
    public List<Ubicacion> obtenerHistorialPorRepartidor(Long repartidorId) {
        // Validamos la existencia antes de intentar buscar su historial
        repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + repartidorId));

        return ubicacionRepository.findByRepartidorIdOrderByFechaHoraDesc(repartidorId);
    }

    @Override
    public void limpiarHistorialDelRepartidor(Long repartidorId) {
        repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + repartidorId));

        ubicacionRepository.deleteByRepartidorId(repartidorId);
    }
}