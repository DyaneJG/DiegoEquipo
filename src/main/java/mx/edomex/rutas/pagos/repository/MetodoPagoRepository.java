package mx.edomex.rutas.pagos.repository;

import mx.edomex.rutas.pagos.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 * Acceso a la tabla metodos_pago.
 */
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, UUID> {

    // Busca por código (codi, stripe, etc.)
    Optional<MetodoPago> findByCodigo(String codigo);

}