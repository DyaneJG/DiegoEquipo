package mx.edomex.rutas.pedidos.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edomex.rutas.pedidos.dto.PedidoFilter;
import mx.edomex.rutas.pedidos.dto.PedidoRequest;
import mx.edomex.rutas.pedidos.dto.PedidoResponse;
import mx.edomex.rutas.pedidos.exception.PedidoNotFoundException;
import mx.edomex.rutas.pedidos.exception.PedidoValidationException;
import mx.edomex.rutas.pedidos.model.EstadoPedido;
import mx.edomex.rutas.pedidos.model.Pedido;
import mx.edomex.rutas.pedidos.repository.PedidoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PedidoServiceImplementation implements PedidoService{

  private final PedidoRepository pedidoRepository;
  private static final String PEDIDO_NOT_FOUND_MSG = "Pedido no encontrado con Id: ";
  // crear pedido
  @Override
  public PedidoResponse crearPedido(PedidoRequest request) {
    log.info("creando un nuevo pedido para cliente {}", request.getClienteId());

    validarCreacionPedido(request);
    
    // Creo un numero de pe4dido unico
    String numeroPedido = generarNumeroPedido();

    // Calcular el total
    BigDecimal total = calcularTotal(request.getSubtotal(), request.getIva(), request.getCargoEntrega());

    Pedido pedido = Pedido.builder().numeroPedido(numeroPedido).clienteId(request.getClienteId()).direccionEntregaId(request.getDireccionEntregaId()).estado(EstadoPedido.PENDIENTE).subtotal(request.getSubtotal()).iva(request.getIva()).cargoEntrega(request.getCargoEntrega() != null ? request.getCargoEntrega() : BigDecimal.ZERO).total(total).notas(request.getNotas()).ventanaInicio(request.getVentanaInicio()).ventanaFin(request.getVentanaFin()).fechaEntregaProgramada(request.getFechaEntregaProgramada()).build();

    Pedido pedidoGuardado = pedidoRepository.save(pedido);
    log.info("pedido creado exitosamente con numero : {}", pedidoGuardado.getNumeroPedido());

    return convertirAResponseDTO(pedidoGuardado);
  }
  // obtener mi pedido
  public PedidoResponse obtenerPedidoPorId(UUID id) {
    log.debug("Buscando Pedido por ID : {}", id);
    Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException(PEDIDO_NOT_FOUND_MSG + id));
    
    return convertirAResponseDTO(pedido);
  }

  // obtengo mi pedido por numero de pedido
  @Override
  public PedidoResponse obtenerPedidoPorNumero(String numeroPedido) {
    log.debug("buscando peidod por numero: {}", numeroPedido);
    Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido).orElseThrow(() -> new PedidoNotFoundException(numeroPedido, true));

    return convertirAResponseDTO(pedido);
  }

  // actualizar mis pedidos
  @Override
  public PedidoResponse actualizarPedido(UUID id, PedidoRequest request) {
    log.info("actualizando pedido ID: {}", id);

    Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException(PEDIDO_NOT_FOUND_MSG + id));

    // valido si el pedido puede ser actualizado
    validarActualizacionPedido(pedidoExistente);

    // actualizar campos de pedidos
    pedidoExistente.setDireccionEntregaId(request.getDireccionEntregaId());
    pedidoExistente.setSubtotal(request.getSubtotal());
    pedidoExistente.setIva(request.getIva());
    pedidoExistente.setCargoEntrega(request.getCargoEntrega() != null ? request.getCargoEntrega() : BigDecimal.ZERO);
    pedidoExistente.setTotal(calcularTotal(request.getSubtotal(), request.getIva(), request.getCargoEntrega()));
    pedidoExistente.setVentanaInicio(request.getVentanaInicio());
    pedidoExistente.setVentanaFin(request.getVentanaFin());
    pedidoExistente.setNotas(request.getNotas());
    pedidoExistente.setFechaEntregaProgramada(request.getFechaEntregaProgramada());

    Pedido pedidoActualizado = pedidoRepository.save(pedidoExistente);
    log.info("Pedido actualizado correctamente ID: {}", id);

    return convertirAResponseDTO(pedidoActualizado);
  }

  // cambiar el estado de mi pedido
  @Override
  public PedidoResponse cambiarEstado(UUID id, EstadoPedido nuevoEstado) {
    log.info("Cambiando el estado del pedido {} a {}", id, nuevoEstado);
    Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException(PEDIDO_NOT_FOUND_MSG + id));

    validarCambioEstado(pedido.getEstado(), nuevoEstado);
    pedido.setEstado(nuevoEstado);

    if (nuevoEstado == EstadoPedido.ENTREGADO) {
      
      pedido.setFechaEntregaReal(OffsetDateTime.now());
    }

     Pedido pedidoActualizado = pedidoRepository.save(pedido);
     log.info("estado del pedido {} actualizado a {}",id, nuevoEstado);

     return convertirAResponseDTO(pedidoActualizado);

  }

  // cancelar un pedido
  @Override
  public void cancelarPedido(UUID id, String motivo) {

    log.info("cancelando pedido {}, motivo: {}",id, motivo);

    Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new PedidoValidationException(PEDIDO_NOT_FOUND_MSG + id));
    
    if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
      throw new PedidoValidationException("no se puede cancelar un pedido ya entregado o cancelado");
    }

    pedido.setEstado(EstadoPedido.CANCELADO);
    pedido.setCanceladoMotivo(motivo);

    pedidoRepository.save(pedido);
    log.info("pedido cancelado exitosamente");

  }

  // eliminar un pedido
  @Override
  public void eliminarPedido(UUID id) {
    log.info("Eliminando pedido ID: {}", id);

    if (!pedidoRepository.existsById(id)) {
      throw new PedidoNotFoundException(PEDIDO_NOT_FOUND_MSG + id);
    }

    pedidoRepository.deleteById(id);
    log.info("Pedido {} eliminado correctamente", id);
  }

   @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPedidos(Pageable pageable) {
        log.debug("Listando pedidos paginados");
        return pedidoRepository.findAll(pageable)
                .map(this::convertirAResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorCliente(UUID clienteId) {
        log.debug("Listando pedidos del cliente: {}", clienteId);
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorEstado(EstadoPedido estado) {
        log.debug("Listando pedidos por estado: {}", estado);
        return pedidoRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Listando pedidos entre fechas: {} - {}", fechaInicio, fechaFin);
        return pedidoRepository.findByFechaEntregaProgramadaBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> filtrarPedidos(PedidoFilter filtro, Pageable pageable) {
        log.debug("Filtrando pedidos con criterios: {}", filtro);
        // Implementar filtros dinámicos (se puede usar Specification o Query DSL)
        // Por ahora devolvemos todos los pedidos
        return pedidoRepository.findAll(pageable)
                .map(this::convertirAResponseDTO);
    }

    @Override
    public PedidoResponse marcarComoEntregado(UUID id) {
        log.info("Marcando pedido {} como entregado", id);
        return cambiarEstado(id, EstadoPedido.ENTREGADO);
    }

    @Override
    public PedidoResponse asignarRepartidor(UUID id, UUID repartidorId) {
        // Este método se implementará cuando se integre con el módulo de repartidores
        log.info("Asignando repartidor {} al pedido {}", repartidorId, id);
        
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(PEDIDO_NOT_FOUND_MSG + id));
        
        // Aquí se integrará con el servicio de repartidores
        
        return convertirAResponseDTO(pedido);
    }

  // convertir a mi response dto
  private PedidoResponse convertirAResponseDTO(Pedido pedido) {
    return PedidoResponse.builder().id(pedido.getId()).numeroPedido(pedido.getNumeroPedido()).clienteId(pedido.getClienteId()).direccionEntregaId(pedido.getDireccionEntregaId()).estado(pedido.getEstado()).subtotal(pedido.getSubtotal()).iva(pedido.getIva()).cargoEntrega(pedido.getCargoEntrega()).total(pedido.getTotal()).notas(pedido.getNotas()).ventanaInicio(pedido.getVentanaInicio()).ventanaFin(pedido.getVentanaFin()).fechaEntregaProgramada(pedido.getFechaEntregaProgramada()).fechaEntregaReal(pedido.getFechaEntregaReal()).canceladoMotivo(pedido.getCanceladoMotivo()).creadoEn(pedido.getCreadoEn()).actualizadoEn(pedido.getActualizadoEn()).build();
  }

  // valido la creacion del pedido
  private void validarCreacionPedido(PedidoRequest request) {

    if (request.getClienteId() == null) {
      throw new PedidoValidationException("El cliente es requerido");
    }

    if (request.getDireccionEntregaId() == null) {
      throw new PedidoValidationException("la direccion de entrega es requerida");
    }

    if (request.getSubtotal() == null || request.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
      throw new PedidoValidationException("El subtotal debe ser mayor a 0");
    }

    if (request.getIva() == null || request.getIva().compareTo(BigDecimal.ZERO) < 0) {
      throw new PedidoValidationException("El iva debe ser mayor a 0");
    }
  }

  // valido si es posible actualizar el pedido
  private void validarActualizacionPedido(Pedido pedido) {
    if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
      throw new PedidoValidationException("no se pede actualizar un pedido ya entregado o cancelado");
    }
  }

  // valido si el pedido no esta cancelado o entregado
  private void validarCambioEstado(EstadoPedido estadoActual, EstadoPedido nuevoEstado) {
    if (estadoActual == EstadoPedido.CANCELADO || estadoActual == EstadoPedido.ENTREGADO) {
      throw new PedidoValidationException("no se puede cambiar el estado del pedido " + estadoActual);
    }
    if (estadoActual == nuevoEstado) {
      throw new PedidoValidationException("el pedido ya se encuentra en " + nuevoEstado);
    }
  }

  private String generarNumeroPedido() {
    // Generar numero de pedido unico
    String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefijo = "PED-" + fechaStr + "-";
    
    long conteoHoy = pedidoRepository.countByNumeroPedidoStartingWith(prefijo);
    long siguenteNumero = conteoHoy + 1;
    return prefijo + String.format("%04d", siguenteNumero);
  }

  private BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal iva, BigDecimal cargoEntrega)  {
    BigDecimal cargo = cargoEntrega != null ? cargoEntrega: BigDecimal.ZERO;
    return subtotal.add(iva).add(cargo);
  }
}
