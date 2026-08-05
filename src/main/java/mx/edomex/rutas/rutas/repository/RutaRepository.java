package mx.edomex.rutas.rutas.repository;

import mx.edomex.rutas.rutas.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    // Este método buscará la ruta que le pertenezca a ese pedido en específico
    Optional<Ruta> findByPedidoId(Long pedidoId);
}