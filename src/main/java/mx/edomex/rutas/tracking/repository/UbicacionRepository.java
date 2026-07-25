package mx.edomex.rutas.tracking.repository;

import mx.edomex.rutas.tracking.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    // Busca por ID del repartidor y lo ordena por fecha descendente
    List<Ubicacion> findByRepartidorIdOrderByFechaHoraDesc(Long repartidorId);
}