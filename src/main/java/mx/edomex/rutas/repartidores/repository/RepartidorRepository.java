package mx.edomex.rutas.repartidores.repository;

import mx.edomex.rutas.repartidores.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// no tocar esta interfaz
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    List<Repartidor> findByEstado(String estado);
}