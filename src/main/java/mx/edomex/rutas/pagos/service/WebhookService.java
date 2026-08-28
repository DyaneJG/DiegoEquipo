package mx.edomex.rutas.pagos.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edomex.rutas.pagos.exception.PaymentException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
  private final PaymentService paymentService;
  
  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @Transactional
  public void processWebhook (String payload, String sigHeader) {
    try {
      // verificar la firma del webhook
      Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
      log.info("evento recibido de Stripe: {}", event.getType());

      // procesar segun el tipo de evento
      switch (event.getType()) {
        case "payment_intent.succeeded":
          handlePaymentIntentSucceeded(event);
          break;
        case "payment_intent.payment_failed":
          handlePaymentIntentFailed(event);
          break;
        case "payment_intent.canceled":
          handlePaymentIntentCanceled(event);
          break;
        case "charge.refunded":
          handleChargeRefunded(event);
          break;
        default:
          break;
      }
    } catch (Exception e) {
      log.error("Error procesando el webhook: {}", e.getMessage());
      throw new PaymentException("Error procesando el webhook:"  + e.getMessage());
    }
  }

  private void handlePaymentIntentSucceeded(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

    if (paymentIntent != null) {
      log.info("PaymentIntent succeeded: {}", paymentIntent.getId());
      paymentService.confirmPayment(paymentIntent.getId());
    }
  }
  
  private void handlePaymentIntentFailed(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

    if (paymentIntent != null) {
      log.warn("PaymentIntent failed: {}", paymentIntent.getId());
    }
  }

  private void handlePaymentIntentCanceled(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

    if (paymentIntent != null) {
      log.info("PaymentIntent cancelado: {}", paymentIntent.getId());
    }
  }

  private void handleChargeRefunded(Event event) {
    log.info("reembolso recibido");
  }
}