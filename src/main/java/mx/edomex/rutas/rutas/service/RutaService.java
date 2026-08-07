package mx.edomex.rutas.rutas.service;

import java.util.UUID;
import mx.edomex.rutas.pedidos.model.EstadoPedido;
import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.pedidos.repository.PedidoRepository;
import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import mx.edomex.rutas.rutas.model.Ruta;
import mx.edomex.rutas.rutas.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Transactional
    public Ruta asignarRepartidorAPedido(UUID pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró ningún pedido con el ID " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new RuntimeException("Error: El pedido no se puede asignar porque su estado es " + pedido.getEstado());
        }

        Repartidor repartidorLibre = repartidorRepository.findFirstByEstado("DISPONIBLE")
                .orElseThrow(() -> new RuntimeException("Alerta: No hay repartidores disponibles en este momento."));

        Ruta nuevaRuta = new Ruta();
        nuevaRuta.setPedido(pedido);
        nuevaRuta.setRepartidor(repartidorLibre);
        nuevaRuta.setEstado("EN_PROGRESO");

        // ELIMINADO: pedido.setRepartidor(repartidorLibre); <- Ya no ensuciamos su modelo

        repartidorLibre.setEstado("NO DISPONIBLE");

        repartidorRepository.save(repartidorLibre);
        pedidoRepository.save(pedido);

        return rutaRepository.save(nuevaRuta);
    }

    @Transactional
    public String finalizarEntrega(UUID pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el pedido con ID " + pedidoId));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("El pedido ya fue entregado previamente.");
        }

        // NUEVO ORDEN: Primero buscamos la ruta, y de ahí sacamos al repartidor
        Ruta rutaActiva = rutaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la ruta activa para este pedido."));

        // Extraemos al repartidor directamente desde la relación de tu Ruta
        Repartidor repartidor = rutaActiva.getRepartidor();
        if (repartidor == null) {
            throw new RuntimeException("Error interno: La ruta activa no tiene un repartidor asignado.");
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        repartidor.setEstado("DISPONIBLE");
        rutaActiva.setEstado("COMPLETADA");

        pedidoRepository.save(pedido);
        repartidorRepository.save(repartidor);
        rutaRepository.save(rutaActiva);

        return "¡Entrega finalizada! El pedido " + pedidoId + " fue entregado, la ruta se completó y " + repartidor.getNombreCompleto() + " está libre de nuevo.";
    }
}