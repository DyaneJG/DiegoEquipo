package mx.edomex.rutas.rutas.service;

import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.rutas.model.Ruta;
import mx.edomex.rutas.rutas.repository.RutaRepository;
// Aquí tendrás que importar tus repositorios de Pedidos y Repartidores
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    // TODO: Necesitaremos inyectar el RepartidorRepository y el PedidoRepository aquí

    public Ruta asignarRepartidorAPedido(Long pedidoId) {

        // 1. Buscar el pedido por su ID en la base de datos
        // 2. Verificar que su estado sea "PAGADO"
        // 3. Buscar en la base de datos al primer Repartidor que esté "DISPONIBLE"
        // 4. Crear una nueva Ruta vinculando ese Pedido y ese Repartidor
        // 5. Cambiar el estado del Repartidor a "OCUPADO"
        // 6. Guardar la nueva Ruta y el Repartidor actualizado en la base de datos

        return null; // Cambiaremos esto cuando llenemos la lógica
    }
}