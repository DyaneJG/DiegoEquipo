package mx.edomex.rutas.pedidos.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNotFoundException extends RuntimeException {
  public PedidoNotFoundException(String message) {
    super(message);
  }

  public PedidoNotFoundException(UUID id) {
    super("Pedido no encontrado con el ID: "  + id);
  }
  
  public PedidoNotFoundException(String numeroPedido, boolean porNumero) {
    super("Pedido no encontrado con el numero de pedido:" + numeroPedido);
  }

}
