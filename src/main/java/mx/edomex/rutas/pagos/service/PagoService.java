package mx.edomex.rutas.pagos.service;

import mx.edomex.rutas.pagos.dto.CrearPagoRequest;
import mx.edomex.rutas.pagos.dto.PagoResponse;
import mx.edomex.rutas.pagos.model.EstadoPago;
import mx.edomex.rutas.pagos.model.MetodoPago;
import mx.edomex.rutas.pagos.model.Pago;
import mx.edomex.rutas.pagos.repository.MetodoPagoRepository;
import mx.edomex.rutas.pagos.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final ProveedorPago proveedorPago;
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

    public PagoResponse crearPagoCoDi(CrearPagoRequest request) {

        MetodoPago metodo = metodoPagoRepository
                .findByCodigo("codi")
                .orElseThrow(() ->
                        new RuntimeException("Método CoDi no encontrado"));

        String referencia = proveedorPago.generarReferencia();
        String contenidoQR = proveedorPago.generarContenidoQR(referencia, request.getMonto());
        String qr = qrService.generarQR(contenidoQR);

        Pago pago = new Pago();
        pago.setPedidoId(request.getPedidoId());

        pago.setMetodoPagoId(metodo.getId());

        pago.setProveedor("CoDi");

        pago.setEstado(EstadoPago.PENDIENTE.name());

        pago.setMonto(request.getMonto());
        pago.setReferenciaExterna(referencia);
        pago.setQrCodigo(qr);

        pago.setQrExpiraAt(OffsetDateTime.now().plusMinutes(15));

        pagoRepository.save(pago);

        PagoResponse response = new PagoResponse();
        response.setId(pago.getId());

        response.setEstado(pago.getEstado());

        response.setReferencia(pago.getReferenciaExterna());
        response.setQr(pago.getQrCodigo());
        response.setExpira(pago.getQrExpiraAt().toLocalDateTime());

        return response;
    }

    public void confirmarPago(String referencia) {

        Pago pago = pagoRepository
                .findByReferenciaExterna(referencia)
                .orElseThrow(() ->
                        new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.PAGADO.name());
        pago.setPagadoAt(OffsetDateTime.now());

        pagoRepository.save(pago);
    }
}