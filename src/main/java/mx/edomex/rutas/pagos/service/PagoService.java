package mx.edomex.rutas.pagos.service;

import mx.edomex.rutas.pagos.dto.CrearPagoRequest;
import mx.edomex.rutas.pagos.dto.PagoResponse;
import mx.edomex.rutas.pagos.model.EstadoPago;
import mx.edomex.rutas.pagos.model.MetodoPago;
import mx.edomex.rutas.pagos.model.Pago;
import mx.edomex.rutas.pagos.repository.MetodoPagoRepository;
import mx.edomex.rutas.pagos.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PagoService {

    // Repositorio de pagos
    private final PagoRepository pagoRepository;

    // Repositorio de métodos
    private final MetodoPagoRepository metodoPagoRepository;

    // Servicio CoDi
  private final ProveedorPago proveedorPago;

    // Servicio QR
    private final QRService qrService;

    public PagoService(
            PagoRepository pagoRepository,
            MetodoPagoRepository metodoPagoRepository,
            ProveedorPago proveedorPago,
            QRService qrService
    ) {
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.proveedorPago = proveedorPago;
        this.qrService = qrService;
    }

    /*
     * Crea un pago CoDi.
     */
    public PagoResponse crearPagoCoDi(CrearPagoRequest request) {

        // Busca el método CoDi
        MetodoPago metodo = metodoPagoRepository
                .findByCodigo("codi")
                .orElseThrow(() ->
                        new RuntimeException("Método CoDi no encontrado"));

        // Genera referencia
        SString referencia =
        proveedorPago.generarReferencia();

String contenidoQR =
        proveedorPago.generarContenidoQR(
                referencia,
                request.getMonto());

String qr =
        qrService.generarQR(
                contenidoQR);

        // Crea el pago
        Pago pago = new Pago();

        pago.setPedidoId(request.getPedidoId());
        pago.setMetodoPago(metodo);
        pago.setProveedor("CoDi");
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMonto(request.getMonto());
        pago.setReferenciaExterna(referencia);
        pago.setQrCodigo(qr);
        pago.setQrExpiraAt(LocalDateTime.now().plusMinutes(15));

        // Guarda en BD
        pagoRepository.save(pago);

        // Respuesta
        PagoResponse response = new PagoResponse();

        response.setId(pago.getId());
        response.setEstado(pago.getEstado().name());
        response.setReferencia(pago.getReferenciaExterna());
        response.setQr(pago.getQrCodigo());
        response.setExpira(pago.getQrExpiraAt());

        return response;

    }

    /*
     * Confirma un pago.
     */
    public void confirmarPago(String referencia) {

        Pago pago = pagoRepository
                .findByReferenciaExterna(referencia)
                .orElseThrow(() ->
                        new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.PAGADO);
        pago.setPagadoAt(LocalDateTime.now());

        pagoRepository.save(pago);

    }

}