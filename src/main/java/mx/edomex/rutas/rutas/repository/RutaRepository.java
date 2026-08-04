package mx.edomex.rutas.rutas.repository;

import mx.edomex.rutas.rutas.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {


}