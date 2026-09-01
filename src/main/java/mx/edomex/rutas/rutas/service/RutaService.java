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

/**
 * Servicio de Rutas
 * Contiene el núcleo de la lógica de negocio logística, coordina pedidos, repartidores y rutas
 */
@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RepartidorRepository repartidorRepository;

    /**
     * @Transactional. Esta función modifica tres tablas distintas
     * Si ocurre un error al guardar la nueva ruta, la anotación deshace
     * el cambio de estado del repartidor y del pedido. O se guarda todo, o no se guarda nada
     */
    @Transactional
    public Ruta asignarRepartidorAPedido(UUID pedidoId) {

        // 1. Validar que el pedido exista en la base de datos
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró ningún pedido con el ID " + pedidoId));

        // 2. Regla de Negocio: Solo podemos despachar pedidos que ya estén preparados y confirmados
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new RuntimeException("Error: El pedido no se puede asignar porque su estado es " + pedido.getEstado());
        }

        // 3. Sistema de colas simple: Tomamos al primer repartidor que esté libre.
        Repartidor repartidorLibre = repartidorRepository.findFirstByEstado("DISPONIBLE")
                .orElseThrow(() -> new RuntimeException("Alerta: No hay repartidores disponibles en este momento."));

        // 4. Creamos el vínculo (Ruta)
        Ruta nuevaRuta = new Ruta();
        nuevaRuta.setPedido(pedido);
        nuevaRuta.setRepartidor(repartidorLibre);
        nuevaRuta.setEstado("EN_PROGRESO");

        // 5. Actualizamos estados para "bloquear" al repartidor
        repartidorLibre.setEstado("NO DISPONIBLE");

        // 6. Persistencia (Guardamos los cambios en las 3 entidades)
        repartidorRepository.save(repartidorLibre);
        pedidoRepository.save(pedido);

        return rutaRepository.save(nuevaRuta);
    }

    /**
     * Cierra el ciclo de vida de la entrega y libera los recursos
     * También transaccional para garantizar integridad de datos
     */
    @Transactional
    public String finalizarEntrega(UUID pedidoId) {

        // 1. Validaciones previas
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el pedido con ID " + pedidoId));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("El pedido ya fue entregado previamente.");
        }

        // 2. Rastreamos la ruta activa usando el ID del pedido
        Ruta rutaActiva = rutaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la ruta activa para este pedido."));

        // 3. Obtenemos al empleado responsable
        Repartidor repartidor = rutaActiva.getRepartidor();
        if (repartidor == null) {
            throw new RuntimeException("Error interno: La ruta activa no tiene un repartidor asignado.");
        }

        // 4. Actualizamos el estado de toda la maquinaria a modo "Finalizado"
        pedido.setEstado(EstadoPedido.ENTREGADO);
        repartidor.setEstado("DISPONIBLE"); // ¡Importante! El repartidor vuelve a la fila de disponibles
        rutaActiva.setEstado("COMPLETADA");

        // 5. Guardamos en Base de Datos
        pedidoRepository.save(pedido);
        repartidorRepository.save(repartidor);
        rutaRepository.save(rutaActiva);

        return "¡Entrega finalizada! El pedido " + pedidoId + " fue entregado, la ruta se completó y " + repartidor.getNombreCompleto() + " está libre de nuevo.";
    }
}