package mx.edomex.rutas.auth.repository;

import mx.edomex.rutas.auth.model.Usuario;
import mx.edomex.rutas.shared.audit.BaseAuditRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends BaseAuditRepository<Usuario, UUID> {
    Optional<Usuario> findBycorreoAndActivoTrue(String correo);
    boolean existsByCorreo(String corrreo);
}
