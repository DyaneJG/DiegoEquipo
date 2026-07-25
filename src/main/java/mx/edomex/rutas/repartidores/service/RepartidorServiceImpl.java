package mx.edomex.rutas.repartidores.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service // lógica
public class RepartidorServiceImpl implements RepartidorService {

    private final RepartidorRepository repository;

    // inyección de dependencias por constructor
    public RepartidorServiceImpl(RepartidorRepository repository) {
        this.repository = repository;
    }

    @Override
    public Repartidor registrarRepartidor(Repartidor repartidor) {
        repartidor.setEstado("DISPONIBLE"); //la lógica de negocio por defecto
        return repository.save(repartidor);
    }

    @Override
    public List<Repartidor> obtenerRepartidoresDisponibles() {
        return repository.findByEstado("DISPONIBLE");
    }

    // Método PUT
    @Override
    public Repartidor actualizarRepartidor(Long id, Repartidor detallesRepartidor) {
        // 1. Buscando repartidor en la BD
        Repartidor repartidorExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + id));

        // 2. Actualizamos los datos con el JSON
        repartidorExistente.setNombreCompleto(detallesRepartidor.getNombreCompleto());
        repartidorExistente.setTelefono(detallesRepartidor.getTelefono());
        repartidorExistente.setPlacaVehiculo(detallesRepartidor.getPlacaVehiculo());

        // 3. Permitimos la modificación del estado desde el JSON
        repartidorExistente.setEstado(detallesRepartidor.getEstado());

        // 4. Guardamos los cambios en PostgreSQL
        return repository.save(repartidorExistente);
    }
}