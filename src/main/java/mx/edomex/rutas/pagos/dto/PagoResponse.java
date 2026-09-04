package mx.edomex.rutas.pagos.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Respuesta enviada al cliente.
 */
public class PagoResponse {

    private UUID id;

    private String estado;

    private String referencia;

    private String qr;

    private LocalDateTime expira;

    public PagoResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public LocalDateTime getExpira() {
        return expira;
    }

    public void setExpira(LocalDateTime expira) {
        this.expira = expira;
    }
}