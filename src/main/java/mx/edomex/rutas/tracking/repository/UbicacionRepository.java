package mx.edomex.rutas.tracking.repository;

import mx.edomex.rutas.tracking.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    /**
     * Spring Data JPA genera el query de SQL automáticamente leyendo el nombre del método.
     * Sirve para mostrar la ruta dibujada desde el punto más actual hasta el inicial.
     */
    List<Ubicacion> findByRepartidorIdOrderByFechaHoraDesc(Long repartidorId);

    /**
     * @Transactional asegura que si ocurre un error a mitad del borrado,
     * se haga un "rollback" y no se borren datos a medias, manteniendo la integridad de la bd
     */
    @Transactional
    void deleteByRepartidorId(Long repartidorId);
}