package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import mx.edomex.rutas.tracking.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final RepartidorRepository repartidorRepository;

    // Inyectamos ambos repositorios para buscar al repartidor y guardar la ubicación
    public UbicacionServiceImpl(UbicacionRepository ubicacionRepository, RepartidorRepository repartidorRepository) {
        this.ubicacionRepository = ubicacionRepository;
        this.repartidorRepository = repartidorRepository;
    }

    @Override
    public Ubicacion registrarUbicacion(Long repartidorId, Ubicacion ubicacion) {
        // 1. Buscando al repartidor por su ID
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + repartidorId));

        // 2. Asignando al repartidor la ubicacion
        ubicacion.setRepartidor(repartidor);

        // 3. Forzamos la hora exacta en la que llega el registro
        ubicacion.setFechaHora(LocalDateTime.now());

        // 4. Guardando en SQL
        return ubicacionRepository.save(ubicacion);
    }
    @Override
    public List<Ubicacion> obtenerHistorialPorRepartidor(Long repartidorId) {
        // 1. Verificando que el repartidor exista primero usando el repartidorRepository
        repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + repartidorId));

        // 2. Historial ordenado del más reciente al más antiguo
        return ubicacionRepository.findByRepartidorIdOrderByFechaHoraDesc(repartidorId);
    }
}
