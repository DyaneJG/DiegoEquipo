package mx.edomex.rutas.shared.exception;

import org.springframework.http.HttpStatus;

public class DominioException extends RuntimeException {
    private final HttpStatus status;

    public DominioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
