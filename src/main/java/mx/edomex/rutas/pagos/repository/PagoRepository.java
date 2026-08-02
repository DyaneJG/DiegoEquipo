package mx.edomex.rutas.pagos.repository;

import mx.edomex.rutas.pagos.model.EstadoPago;
import mx.edomex.rutas.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
 * Acceso a la tabla pagos.
 */
public interface PagoRepository extends JpaRepository<Pago, UUID> {

    // Busca pago por pedido
    Optional<Pago> findByPedidoId(UUID pedidoId);

    // Busca por referencia externa
    Optional<Pago> findByReferenciaExterna(String referenciaExterna);

    // Busca pagos por estado
    List<Pago> findByEstado(EstadoPago estado);

}