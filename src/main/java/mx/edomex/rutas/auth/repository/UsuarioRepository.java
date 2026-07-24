package mx.edomex.rutas.auth.repository;

import mx.edomex.rutas.auth.model.Usuario;
import mx.edomex.rutas.shared.audit.BaseAuditRepository;

import java.util.Optional;

public interface UsuarioRepository extends BaseAuditRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndActivoTrue(String email);
    boolean existsByEmail(String email);
}
