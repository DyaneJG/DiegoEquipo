package mx.edomex.rutas.notificaciones.repository;

import mx.edomex.rutas.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    
    // Spring Data JPA crea las consultas SQL automáticamente basándose en el nombre del método
    
    // Buscar todas las notificaciones de un usuario específico
    List<Notificacion> findByUsuarioId(UUID usuarioId);
    
    // Buscar notificaciones por su estado 
    List<Notificacion> findByEstado(String estado);
}