package mx.edomex.rutas.notificaciones.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@EntityListeners(AuditingEntityListener.class) 
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

  
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "pedido_id")
    private UUID pedidoId;

    @Column(length = 20, nullable = false)
    private String canal; 

    @Column(length = 200, nullable = false) 
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false) 
    private String cuerpo;

    @Column(length = 20, nullable = false)
    private String estado; 

    @Column(name = "enviado_at")
    private LocalDateTime enviadoAt;

    @Column(name = "error_detalle", columnDefinition = "TEXT")
    private String errorDetalle;
    
    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    public Notificacion() {}

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public UUID getPedidoId() { return pedidoId; }
    public void setPedidoId(UUID pedidoId) { this.pedidoId = pedidoId; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getEnviadoAt() { return enviadoAt; }
    public void setEnviadoAt(LocalDateTime enviadoAt) { this.enviadoAt = enviadoAt; }

    public String getErrorDetalle() { return errorDetalle; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}