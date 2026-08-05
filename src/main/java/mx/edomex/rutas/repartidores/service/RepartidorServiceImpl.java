package mx.edomex.rutas.repartidores.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import org.mindrot.jbcrypt.BCrypt; // Importación de la librería de seguridad
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
        repartidor.setEstado("DISPONIBLE"); // la lógica de negocio por defecto

        // 1. Encriptamos la contraseña antes de guardar el nuevo repartidor
        if (repartidor.getContrasena() != null && !repartidor.getContrasena().isEmpty()) {
            String passwordEncriptada = BCrypt.hashpw(repartidor.getContrasena(), BCrypt.gensalt());
            repartidor.setContrasena(passwordEncriptada);
        }

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

        repartidorExistente.setCorreo(detallesRepartidor.getCorreo());

        // 3. Permitimos la modificación del estado desde el JSON
        repartidorExistente.setEstado(detallesRepartidor.getEstado());

        // 4. Si el JSON trae una nueva contraseña, la encriptamos y la actualizamos
        if (detallesRepartidor.getContrasena() != null && !detallesRepartidor.getContrasena().isEmpty()) {
            String passwordEncriptada = BCrypt.hashpw(detallesRepartidor.getContrasena(), BCrypt.gensalt());
            repartidorExistente.setContrasena(passwordEncriptada);
        }

        // 5. Guardamos los cambios en PostgreSQL
        return repository.save(repartidorExistente);
    }

    // Método DELETE
    @Override
    public void eliminarRepartidor(Long id) {
        // 1. Buscamos si existe
        Repartidor repartidorExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Repartidor no encontrado con el ID: " + id));

        // 2. Si existe, lo borramos
        repository.delete(repartidorExistente);
    }
}