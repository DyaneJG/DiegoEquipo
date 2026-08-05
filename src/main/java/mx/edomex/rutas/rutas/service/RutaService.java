package mx.edomex.rutas.rutas.service;

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

    @Transactional // Si algo falla, se revierte todo automáticamente
    public Ruta asignarRepartidorAPedido(Long pedidoId) {

        // 1. Buscar el pedido por su ID
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró ningún pedido con el ID " + pedidoId));

        // 2. Verificar que su estado sea "PAGADO"
        if (!"PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new RuntimeException("Error: El pedido no se puede asignar porque su estado es " + pedido.getEstado());
        }

        // 3. Buscar al primer Repartidor "DISPONIBLE"
        Repartidor repartidorLibre = repartidorRepository.findFirstByEstado("DISPONIBLE")
                .orElseThrow(() -> new RuntimeException("Alerta: No hay repartidores disponibles en este momento."));

        // 4. Crear una nueva Ruta vinculando el Pedido y el Repartidor
        Ruta nuevaRuta = new Ruta();
        nuevaRuta.setPedido(pedido);
        nuevaRuta.setRepartidor(repartidorLibre);
        nuevaRuta.setEstado("EN_PROGRESO");

        // (Opcional pero recomendado) Como en tu clase Pedido le pusimos @ManyToOne a Repartidor, lo asignamos también:
        pedido.setRepartidor(repartidorLibre);

        // 5. Cambiar el estado del Repartidor a "NO DISPONIBLE" (como era tu regla de negocio)
        repartidorLibre.setEstado("NO DISPONIBLE");

        // 6. Guardar los cambios en la base de datos
        repartidorRepository.save(repartidorLibre);
        pedidoRepository.save(pedido);

        return rutaRepository.save(nuevaRuta);
    }

    @Transactional
    public String finalizarEntrega(Long pedidoId) {

        // 1. Buscamos el pedido en la base de datos
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el pedido con ID " + pedidoId));

        // 2. Verificamos que no esté entregado ya
        if ("ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new RuntimeException("El pedido ya fue entregado previamente.");
        }

        // 3. Obtenemos al repartidor que traía este pedido
        Repartidor repartidor = pedido.getRepartidor();
        if (repartidor == null) {
            throw new RuntimeException("Este pedido no tiene un repartidor asignado aún.");
        }

        // --- NUEVO PASO: Buscamos la ruta activa ---
        Ruta rutaActiva = rutaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la ruta activa para este pedido."));

        // 4. ¡La magia completa! Cambiamos los TRES estados
        pedido.setEstado("ENTREGADO");
        repartidor.setEstado("DISPONIBLE");
        rutaActiva.setEstado("COMPLETADA"); // Cerramos la ruta

        // 5. Guardamos los cambios de las TRES tablas en PostgreSQL
        pedidoRepository.save(pedido);
        repartidorRepository.save(repartidor);
        rutaRepository.save(rutaActiva);

        return "¡Entrega finalizada! El pedido " + pedidoId + " fue entregado, la ruta se completó y " + repartidor.getNombreCompleto() + " está libre de nuevo.";
    }
}