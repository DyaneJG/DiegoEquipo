package mx.edomex.rutas.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface BaseAuditRepository<T, ID> extends JpaRepository<T, ID> {
    List<T> findByCreadoEnBetween(LocalDateTime desde, LocalDateTime hasta);
}
