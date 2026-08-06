package mx.edomex.rutas.pedidos.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import mx.edomex.rutas.pedidos.model.EstadoPedido;
import mx.edomex.rutas.pedidos.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findByClienteId(UUID clienteId);
    List<Pedido> findByEstado(EstadoPedido estado);

    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

    List<Pedido> findByFechaEntregaProgramadaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT p FROM Pedido p WHERE p.fechaEntregaReal BETWEEN :inicio AND :fin")
    List <Pedido> findByFechaEntregaProgramadaBetween(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);

    @Query("SELECT p FROM Pedido p WHERE p.clienteId = :clienteId AND p.estado = :estado")
    List <Pedido> findByClienteIdAndEstado(@Param("clienteId") UUID clienteId, @Param("estado") EstadoPedido estado);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.numeroPedido LIKE :prefijo%")
    long countByNumeroPedidoStartingWith(@Param("prefijo") String prefijo);

    boolean existsByNumeroPedido(String numeroPedido);

}