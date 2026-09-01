package mx.edomex.rutas.rutas.repository;

import mx.edomex.rutas.rutas.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    /**
     * Spring Data JPA construye automáticamente el query SQL
     * SELECT * FROM rutas WHERE pedido_id = ?
     *
     * Usamos Optional<> para obligarnos a manejar el escenario donde
     * el pedido aún no tiene una ruta asignada, evitando el NullPointerException
     */
    Optional<Ruta> findByPedidoId(UUID pedidoId);
}