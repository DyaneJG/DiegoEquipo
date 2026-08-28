package mx.edomex.rutas.pagos.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edomex.rutas.pagos.dto.PaymentRequest;
import mx.edomex.rutas.pagos.dto.PaymentResponse;
import mx.edomex.rutas.pagos.exception.PaymentException;
import mx.edomex.rutas.pagos.model.MetodoPago;
import mx.edomex.rutas.pagos.model.Pago;
import mx.edomex.rutas.pagos.repository.MetodoPagoRepository;
import mx.edomex.rutas.pagos.repository.PagoRepository;
import mx.edomex.rutas.pedidos.model.EstadoPedido;
import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.pedidos.repository.PedidoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
  private final PagoRepository pagoRepository;
  private final PedidoRepository pedidoRepository;
  private final MetodoPagoRepository metodoPagoRepository;

  @Value("${stripe.default-currency}")
  private String defaultCurrency;

  private static final List<String> SUPPORTED_CURRENCIES = List.of("mxn", "usd", "eur", "gpb");
  private static final BigDecimal MIN_AMOUNT_MXN = new BigDecimal("0.50");
  private static final BigDecimal MIN_AMOUNT_USD = new BigDecimal("0.50");

  // *crear un nuevo pago para pedido

  @Transactional
  public PaymentResponse createPayment(PaymentRequest request) {
    log.info("Creando pago apar pedido  {} con metodo de pago: {}", request.getPedidoId(),
        request.getMetodoPagoCodigo());

    // valido mio pedido
    try {
      // Validar y obtener el pedido
      Pedido pedido = validateAndGetPedido(request.getPedidoId());
      log.info("✅ Pedido encontrado: {}", pedido.getId());

      // valido y obtengo la forma de pago
      MetodoPago metodoPago = validateAndGetMetodoPago(request.getMetodoPagoCodigo());

      if (pagoRepository.existsByPedidoIdAndEstado(request.getPedidoId(), "PENDIENTE")) {
        throw new PaymentException("Ya existe un pago pendiente para este pedido");
      }

      // valido la moneda y el monto
      String moneda = validateCurrency(request.getMoneda());
      validateAmount(request.getMonto(), moneda);

      // validar que el monto coincida con el total del pedido
      if (request.getMonto().compareTo(pedido.getTotal()) != 0) {
        throw new PaymentException("el monto del pago no coincide con el total del pedido");
      }

      // pagar segun el metodo de pago
      PaymentIntent paymentIntent = null;
      String referenciaExterna = null;
      String qrCode = null;
      OffsetDateTime qrExpiraAt = null;
      String estado = "PENDIENTE";

      // procesar mi pago con Stripe
      if ("stripe".equalsIgnoreCase(metodoPago.getCodigo())) {
        paymentIntent = createStripePaymentIntent(request, pedido);
        referenciaExterna = paymentIntent.getId();
        qrCode = "https://qr.stripe.com/" + paymentIntent.getClientSecret();
        qrExpiraAt = OffsetDateTime.now().plusHours(1);
        estado = "PENDIENTE";
        log.info("PaymentIntent Stripe creado: {}", paymentIntent.getId());

      } else if ("efectivo".equalsIgnoreCase(metodoPago.getCodigo())) {
        referenciaExterna = "EFECTIVO" + pedido.getNumeroPedido();
        estado = "PENDIENTE";
        log.info("PaymentIntent stripe creado para  : {}", pedido.getNumeroPedido());

      } else {
        referenciaExterna = metodoPago.getCodigo().toUpperCase() + "-" + pedido.getNumeroPedido();
        estado = "PENDIENTE";
        log.info("Pago con {} registrado para pedido :{}", metodoPago.getNombre(), pedido.getNumeroPedido());
      }

      // intento guardar el pago en la base de datos
      Pago pago = savePayment(request, pedido, metodoPago, referenciaExterna, qrCode, qrExpiraAt, estado);

      // contruir mi respuesta
      return buildPaymentResponse(paymentIntent, pago, metodoPago);
    } catch (StripeException e) {
      log.error("error de Stripe", e.getMessage());
      throw new PaymentException("error al procesar del pago con stripe: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Error al crear el pago: {}", e.getMessage());
      throw new PaymentException("Error al procesar el pago: " + e.getMessage(), e);
    }
  }

  // * confirmar un pago de Stripe
  @Transactional
  public void confirmPayment(String paymentIntentId) {
    
    try {
      // obtengo el PaymentIntent de Stripe
      PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

      if ("requires_confirmation".equals(paymentIntent.getStatus())) {
        log.info("paymentIntentId requiere confirmacion nuevo estado: {}", paymentIntent.getStatus());

        // Para pruebas uso la tarjeta de striep
        PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder().setPaymentMethod("pm_card_visa")
            .build();

        paymentIntent = paymentIntent.confirm(confirmParams);
        log.info("paymentIntent confirmado con la tarjeta de prueba. Nuevo Estado: {}", paymentIntent.getStatus());
      }
      if ("requires_payment_method".equals(paymentIntent.getStatus())) {
        log.info("paymentIntentId requiere confirmacion nuevo estado: {}", paymentIntent.getStatus());

        // Para pruebas uso la tarjeta de striep
        PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder().setPaymentMethod("pm_card_visa")
            .build();

        paymentIntent = paymentIntent.confirm(confirmParams);
        log.info("paymentIntent confirmado con la tarjeta de prueba. Nuevo Estado: {}", paymentIntent.getStatus());
      }

      // busco el pago en la bd
      Pago pago = pagoRepository.findByReferenciaExterna(paymentIntentId)
          .orElseThrow(() -> new PaymentException("pago no encontrado con  referencia" + paymentIntentId));

      // actualizar segun el estado de Stripe
      if ("succeeded".equals(paymentIntent.getStatus())) {
        pago.setEstado("PAGADO");
        pago.setPagadoAt(OffsetDateTime.now());

        // actaulizar el pedido a confirmado
        updatePedidoAfterSuccessfulPayment(pago.getPedidoId());
        pagoRepository.save(pago);
        log.info("PAGO ACTUALIZADO A PAGADO CON ID : {}", paymentIntentId);
      }
       else if ("requires_payment_method".equals(paymentIntent.getStatus())) {

        pago.setEstado("PENDIENTE");
        log.info("Pago {} en estado: {}", paymentIntentId, paymentIntent.getStatus());
        pagoRepository.save(pago);
      }

      log.info("el pago esta en estado: {}", paymentIntent.getStatus());
    } catch (StripeException e) {
      log.error("Error de Stripe al confirmar el pago :{}", e.getMessage());
      throw new PaymentException("Error en Stripe al confirmar: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Error al confirmar el pago: {}", e.getMessage());
      throw new PaymentException("Error al confirmar el pago: " + e.getMessage(), e);
    }
    
  }

  // confirmar un pago en efectivo
  @Transactional
  public void confirmCashPayment(UUID pagoId) {
    log.info("Confirmado pago en efectivo. {}", pagoId);

    Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new PaymentException("Pago no encontrado"));

    if (!"PENDIENTE".equals(pago.getEstado())) {
      throw new PaymentException("El pago no esta pendiente");
    }

    pago.setEstado("PAGADO");
    pago.setPagadoAt(OffsetDateTime.now());
    pagoRepository.save(pago);

    // Actualizar el pedido a confirmado
    updatePedidoAfterSuccessfulPayment(pago.getPedidoId());

    log.info("Pago en efectivo {} confirmado", pagoId);
  }

  // obtener un pago por la referencia externa
  @Transactional(readOnly = true)
  public Pago getPagoByReferencia(String referenciaExterna) {
    return pagoRepository.findByReferenciaExterna(referenciaExterna).orElse(null);
  }

  // obtener todos los pagos de un pedido
  @Transactional(readOnly = true)
  public List<Pago> getPagosByPedido(UUID pedidoId) {
    return pagoRepository.findByPedidoId(pedidoId);
  }

  // obtener los pagos pendientes de un pedido
  @Transactional(readOnly = true)
  public Pago getPendingPaymentByPedido(UUID pedidoId) {
    return pagoRepository.findByPedidoIdAndEstado(pedidoId, "PENDIENTE").orElse(null);
  }

  // obtener el pago activo de un pedido
  @Transactional(readOnly = true)
  public Pago getActivePaymentByPedido(UUID pedidoId) {
    return pagoRepository.findByPedidoIdAndEstado(pedidoId, "requiere_confirmacion").orElse(null);
  }

  // CANCELAR UN PAGO PENDIENTE
  public void cancelPayment(UUID pagoId, String motivo) {
    log.info("cancelando pago {}", pagoId);

    Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new PaymentException("pago no encontrado"));

    if (!"requiere_confirmacion".equals(pago.getEstado())) {

      throw new PaymentException("Solo se pueden cancelar pagos pendientes");

    }

    pago.setEstado("cancelado");
    pagoRepository.save(pago);
  }

  // obtener los metodos de pago activos
  @Transactional(readOnly = true)
  public List<MetodoPago> getMetodosPagoActivos() {
    return metodoPagoRepository.findByActivoTrue();
  }

  // obtener metodo de pago por su codigo
  public MetodoPago getMetodoPagoByCodigo(String codigo) {
    return metodoPagoRepository.findByCodigoAndActivoTrue(codigo)
        .orElseThrow(() -> new PaymentException("metodo de pago no encontrado " + codigo));
  }

  // validaciones
  private Pedido validateAndGetPedido(UUID pedidoId) {
    if (pedidoId == null) {
      throw new PaymentException("El ID del pedido es requerido");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PaymentException("Pedido no encontrado con ID: " + pedidoId));

    if (pedido.getEstado() == EstadoPedido.CANCELADO ||
        pedido.getEstado() == EstadoPedido.ENTREGADO) {
      throw new PaymentException("No se puede pagar un pedido " + pedido.getEstado());
    }

    if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
      throw new PaymentException("El pedido ya ha sido pagado");
    }

    return pedido;
  }

  private MetodoPago validateAndGetMetodoPago(String codigo) {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new PaymentException("el metodo de pago es requerido");
    }

    MetodoPago metodoPago = metodoPagoRepository.findByCodigoAndActivoTrue(codigo.toLowerCase())
        .orElseThrow(() -> new PaymentException("Metodo de pago no encontrado o inactivo" + codigo));

    return metodoPago;
  }

  private String validateCurrency(String moneda) {
    if (moneda == null) {
      return defaultCurrency;
    }
    String monedaLower = moneda.toLowerCase();
    if (!SUPPORTED_CURRENCIES.contains(monedaLower)) {
      throw new PaymentException("Moneda no soportada " + moneda + "Soportadas" + SUPPORTED_CURRENCIES);
    }
    return monedaLower;
  }

  private void validateAmount(BigDecimal monto, String moneda) {
    if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
      throw new PaymentException("El monto debe ser mayor a 0");
    }

    BigDecimal minAmount = getMinAmount(moneda);

    if (monto.compareTo(minAmount) < 0) {
      throw new PaymentException("el monto minimo en " + moneda.toUpperCase() + " es de " + minAmount);
    }
  }

  private BigDecimal getMinAmount(String moneda) {
    return switch (moneda) {
      case "mxn" -> MIN_AMOUNT_MXN;
      case "usd" -> MIN_AMOUNT_USD;
      default -> new BigDecimal("0.50");
    };
  }

  private PaymentIntent createStripePaymentIntent(PaymentRequest request, Pedido pedido) throws StripeException {
    long amountInCents = request.getMonto().multiply(new BigDecimal(100)).longValue();
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(amountInCents)
        .setCurrency(request.getMoneda())
        .setDescription(
            request.getDescripcion() != null ? request.getDescripcion() : "Pago de pedido " + pedido.getNumeroPedido())
        .setAutomaticPaymentMethods(
            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                .setEnabled(true)
                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                .build())
        .setPaymentMethod("pm_card_visa") // Tarjeta de prueba
        .build(); 
    return PaymentIntent.create(params);
  }

  private Pago savePayment(PaymentRequest request, Pedido pedido, MetodoPago metodoPago, String referenciaExterna,
      String qrCode, OffsetDateTime qrExpiraAt, String estado) {
    Pago pago = Pago.builder().pedidoId(pedido.getId()).metodoPagoId(metodoPago.getId())
        .proveedor(metodoPago.getCodigo()).estado(estado).monto(request.getMonto()).referenciaExterna(referenciaExterna)
        .qrCodigo(qrCode).qrExpiraAt(qrExpiraAt).creadoEn(OffsetDateTime.now()).build();

    return pagoRepository.save(pago);
  }

  private PaymentResponse buildPaymentResponse(PaymentIntent paymentIntent, Pago pago, MetodoPago metodoPago) {
    PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder().estado(pago.getEstado())
        .monto(pago.getMonto()).metodoPago(metodoPago.getNombre());

    if (paymentIntent != null) {
      builder.clientSecret(paymentIntent.getClientSecret()).paymentIntentId(paymentIntent.getId());
    }

    if (pago.getQrCodigo() != null) {
      builder.qrCode(pago.getQrCodigo());
    }

    return builder.build();
  }

  private void updatePedidoAfterSuccessfulPayment(UUID pedidoId) {
    try {
      Pedido pedido = pedidoRepository.findById(pedidoId)
          .orElseThrow(() -> new PaymentException("Pedido no encontrado"));

      log.info("📋 Pedido encontrado - Estado actual: {}", pedido.getEstado());

      pedido.setEstado(EstadoPedido.CONFIRMADO);
      pedido = pedidoRepository.saveAndFlush(pedido);

      log.info("✅ Pedido actualizado - Estado: {}", pedido.getEstado());

    } catch (Exception e) {
      log.error("❌ Error actualizando pedido: {}", e.getMessage(), e);
      throw new PaymentException("Error actualizando pedido: " + e.getMessage(), e);
    }
  }
}