package mx.edomex.rutas.pagos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edomex.rutas.pagos.dto.PaymentRequest;
import mx.edomex.rutas.pagos.dto.PaymentResponse;
import mx.edomex.rutas.pagos.exception.PaymentException;
import mx.edomex.rutas.pagos.model.MetodoPago;
import mx.edomex.rutas.pagos.model.Pago;
import mx.edomex.rutas.pagos.service.PaymentService;
import mx.edomex.rutas.pagos.service.WebhookService;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
  private final PaymentService paymentService;
  private final WebhookService webhookService;

  // obtengo metodos de pago activos
  @GetMapping("/metodos")
  public ResponseEntity<List<MetodoPago>> getMetodosPago() {
    try {
      List<MetodoPago> metodos = paymentService.getMetodosPagoActivos();
      return ResponseEntity.ok(metodos);
    } catch (Exception e) {
      log.error("Error obteniendo los metodos de pago: {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // obtengo un meotod de pago por el codigo
  @GetMapping("/metodos/{codigo}")
  public ResponseEntity<MetodoPago> getMetodoPago(@PathVariable("codigo") String codigo) {
    try {
      MetodoPago pago = paymentService.getMetodoPagoByCodigo(codigo);
      return ResponseEntity.ok(pago);
    } catch (Exception e) {
      log.error("Error al obtener el metodo de pago por codigo: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // crear un nuevo pago
  @PostMapping("/crear")
  public ResponseEntity<PaymentResponse> crearPago(@RequestBody PaymentRequest request) {
    log.info("========================================");
        log.info("📥 REQUEST RECIBIDO EN EL CONTROLADOR");
        log.info("📥 Request: {}", request);
        log.info("📥 pedidoId: {}", request.getPedidoId());
        log.info("📥 metodoPagoCodigo: {}", request.getMetodoPagoCodigo());
        log.info("📥 monto: {}", request.getMonto());
        log.info("📥 moneda: {}", request.getMoneda());
        log.info("📥 descripcion: {}", request.getDescripcion());
        log.info("========================================");
    try {
      log.info("Solcitud para crear un pago para pedido {} con metodo {}", request.getPedidoId(),
          request.getMetodoPagoCodigo());

      PaymentResponse response = paymentService.createPayment(request);
      return ResponseEntity.ok(response);
    } catch (PaymentException e) {
      log.error("Error al crear el pago: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(PaymentResponse.builder().estado("ERROR").mensaje(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Error interno: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(PaymentResponse.builder().estado("ERROR").mensaje(e.getMessage()).build());
    }
  }

  // confirmar mi pago con Stripe

  @PostMapping("/confirmar/stripe/{paymentIntentId}")
  public ResponseEntity<String> confirmarPagoStripe(@PathVariable("paymentIntentId") String paymentIntentId) {
    try {
      log.info("Confirmando pago Stripe: {}", paymentIntentId);
      paymentService.confirmPayment(paymentIntentId);
  
      return ResponseEntity.ok("Pago confirmado exitosamente");

    } catch (PaymentException e) {
      log.error("Error al confirmar el pago con Stripe: {}", e.getMessage());
      return ResponseEntity.badRequest().body("Error de pago: " + e.getMessage());

    } catch (Exception e) {
      log.error("Error interno al confirmar el pago_ {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al confirmar el pago");
    }
  }
  // confirmar un pago en efectivo
  @PostMapping("/confirmar/efectivo/{pagoId}")
  public ResponseEntity<String> confirmarPagoEfectivo(@PathVariable("pagoId") UUID pagoId) {
    try {
      log.info("Confirmando pago en efectivo_ {}", pagoId);

      paymentService.confirmCashPayment(pagoId);

      return ResponseEntity.ok("Pago en efectivo recibido correctamente");

    } catch (PaymentException e) {
      log.error("erro confirmando el pago en efectivo: {}", e.getMessage());
      return ResponseEntity.badRequest().body("error de pago " + e.getMessage());

    } catch (Exception e) {
      log.error("error interno al confirmar el pago en efectivo : {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al confirmar el pago");
    }
  }

  // obtener todos los pagos de un pedido 

  @GetMapping("/pedido/{pedidoId}")
  public ResponseEntity<List<Pago>> getPagosByPedido(@PathVariable("pedidoId") UUID pedidoId) {
    try {
      List<Pago> pagos = paymentService.getPagosByPedido(pedidoId);
      return ResponseEntity.ok(pagos);

    } catch (Exception e) {
        log.error("Error obteniendo los pagos por pedido: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  // obtener el pago pendiente de un pedido
  @GetMapping("/pedido/{pedidoId}/pendiente")
  public ResponseEntity<Pago> getPendingPayment(@PathVariable("pedidoId") UUID pedidoId) {
    try {
      log.info("Obteniendo el pago pendiente por el pedido: {}", pedidoId);

      Pago pago = paymentService.getPendingPaymentByPedido(pedidoId);
      return ResponseEntity.ok(pago);

    } catch(Exception e) {
      log.error(" error obteniendo los pagos pendientes por pedido: {}",e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } 

  }

  // obtener un pago por su referencia externa
  @GetMapping("/referencia/{referenciaExterna}")
  public ResponseEntity<Pago> getPagoByReferencia(@PathVariable("referenciaExterna") String referenciaExterna) {
    try {
      log.info("obteniendo pago por referencia : {}", referenciaExterna);
      Pago pago = paymentService.getPagoByReferencia(referenciaExterna);
      if (pago == null) {
        return ResponseEntity.notFound().build();
      } else {
        return ResponseEntity.ok(pago);
      }
    } catch (Exception e) {
      log.error("Error obteniendo e pago por referencia: {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // cancelar un pago
  @PostMapping("/{pagoId}/cancelar")
  public ResponseEntity<String> cancelarPago(@PathVariable("pagoId") UUID pagoId,@RequestParam String motivo) {
    try {
      paymentService.cancelPayment(pagoId, motivo);
      return ResponseEntity.ok("pago cancelado correctamente");

    } catch(PaymentException e) {
      return ResponseEntity.badRequest().body("error: " + e.getMessage());

    } catch (Exception e) {
      log.error("Error cancelando el pago : {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al cancelar el pago");
    }
  }

  /*
    Webhook
  */

  // webhook de Stripe
  @PostMapping("/webhook/stripe")

  public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
      log.info("webhook recibido de Stripe");

      webhookService.processWebhook(payload, sigHeader);
      return ResponseEntity.ok("Webhook procesado correctamente");

    } catch (PaymentException e) {
      log.error("Error procesando el webhook: {}", e.getMessage());

      return ResponseEntity.badRequest().body("Error con el webhook" + e.getMessage());

    } catch (Exception e) {
      log.error("Error interno procesando el webhook: {}", e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al procesar el webhook");
    }
  }
}
