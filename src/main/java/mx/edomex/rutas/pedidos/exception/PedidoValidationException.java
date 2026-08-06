package mx.edomex.rutas.pedidos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PedidoValidationException extends RuntimeException {
  
  public PedidoValidationException(String message) {
    super(message);
  }

}
