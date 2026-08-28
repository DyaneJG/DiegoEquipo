package mx.edomex.rutas.pagos.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mx.edomex.rutas.pagos.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, UUID> {
  Optional<Pago> findByReferenciaExterna(String referenciaExterna);

  List<Pago> findByPedidoId(UUID pedidoId);

  List<Pago> findByEstado(String estado);

  Optional<Pago> findByPedidoIdAndEstado(UUID pedidoId, String estado);

  boolean existsByPedidoIdAndEstado(UUID pedidoId, String estado);

  @Query("SELECT p FROM Pago p WHERE p.pedidoId = :pedidoId AND p.estado IN :estados")
  List<Pago> findByPedidoIdAndEstados(@Param("pedidoId") UUID pedidoId, @Param("estados") List<String> estados);

  @Query("SELECT COUNT(p) FROM Pago p WHERE p.pedidoId = :pedidoId AND p.estado = 'PAGADO'")
  long countPagadosByPedidoId(@Param("pedidoId") UUID pedidoId);

  @Query("SELECT p FROM Pago p WHERE p.estado = 'PENDIENTE' AND p.creadoEn < :fechaLimite")
  List<Pago> findPagosPendientesExpirados(@Param("fechaLimite") OffsetDateTime fechaLimite);

}
