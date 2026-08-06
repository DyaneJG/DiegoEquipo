package mx.edomex.rutas.pedidos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mx.edomex.rutas.pedidos.dto.PedidoFilter;
import mx.edomex.rutas.pedidos.dto.PedidoRequest;
import mx.edomex.rutas.pedidos.dto.PedidoResponse;
import mx.edomex.rutas.pedidos.model.EstadoPedido;

public interface PedidoService {
  PedidoResponse crearPedido(PedidoRequest request);
  PedidoResponse obtenerPedidoPorId(UUID id);
  PedidoResponse obtenerPedidoPorNumero(String numeroPedido);
  PedidoResponse actualizarPedido(UUID id, PedidoRequest request);
  PedidoResponse cambiarEstado(UUID id, EstadoPedido estado);

  void cancelarPedido(UUID id, String motivo);
  void eliminarPedido(UUID id);

  Page<PedidoResponse> listarPedidos(Pageable pageable);
  List<PedidoResponse> listarPedidosPorCliente(UUID clienteId);
  List<PedidoResponse> listarPedidosPorEstado(EstadoPedido estado);
  List<PedidoResponse> listarPedidosPorFechas(LocalDate fechaInicio, LocalDate fechaFin);

  Page<PedidoResponse> filtrarPedidos(PedidoFilter  filtro, Pageable pageable);
  PedidoResponse marcarComoEntregado(UUID id);
  PedidoResponse asignarRepartidor(UUID id, UUID repartidorId);
  
}
