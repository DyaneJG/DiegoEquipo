package mx.edomex.rutas.pagos.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metodos_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPago {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "codigo", length = 30, nullable = false, unique = true)
  private String codigo;

  @Column(name = "nombre", length = 100, nullable = false)
  private String nombre;

  @Column(name = "requiere_token")
  @Builder.Default
  private Boolean requiereToken = false;

  @Column(name = "activo")
  @Builder.Default
  private Boolean activo = true;
}
