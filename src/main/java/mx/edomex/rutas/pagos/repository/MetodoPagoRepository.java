package mx.edomex.rutas.pagos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.edomex.rutas.pagos.model.MetodoPago;
import java.util.List;


@Repository
public interface MetodoPagoRepository  extends JpaRepository<MetodoPago, UUID>{
  Optional<MetodoPago> findByCodigo(String codigo);
  Optional<MetodoPago> findByCodigoAndActivoTrue(String codigo);

  List<MetodoPago> findByActivoTrue();

  boolean existsByCodigoAndActivoTrue(String codigo);
}