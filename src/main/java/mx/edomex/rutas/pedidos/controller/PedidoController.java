package mx.edomex.rutas.pedidos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edomex.rutas.pedidos.dto.PedidoFilter;
import mx.edomex.rutas.pedidos.dto.PedidoRequest;
import mx.edomex.rutas.pedidos.dto.PedidoResponse;
import mx.edomex.rutas.pedidos.model.EstadoPedido;
import mx.edomex.rutas.pedidos.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {
  private final PedidoService pedidoService;

  @PostMapping
  // TODO: solucionar error que impide realizar más un registro en la bd
  public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody PedidoRequest request) {
    log.info("Solicitud POST /api/pedidos - Crear pedido");
    PedidoResponse response = pedidoService.crearPedido(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedidoPorId(@PathVariable UUID id) {
        log.info("Solicitud GET /api/pedidos/{}", id);
        PedidoResponse response = pedidoService.obtenerPedidoPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<PedidoResponse> obtenerPedidoPorNumero(@PathVariable String numeroPedido) {
        log.info("Solicitud GET /api/pedidos/numero/{}", numeroPedido);
        PedidoResponse response = pedidoService.obtenerPedidoPorNumero(numeroPedido);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> actualizarPedido(
            @PathVariable UUID id,
            @Valid @RequestBody PedidoRequest request) {
        log.info("Solicitud PUT /api/pedidos/{}", id);
        PedidoResponse response = pedidoService.actualizarPedido(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoPedido estado) {
        log.info("Solicitud PATCH /api/pedidos/{}/estado?estado={}", id, estado);
        PedidoResponse response = pedidoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPedido(
            @PathVariable UUID id,
            @RequestParam(required = true) String motivo) {
        log.info("Solicitud PATCH /api/pedidos/{}/cancelar", id);
        pedidoService.cancelarPedido(id, motivo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponse> marcarComoEntregado(@PathVariable UUID id) {
        log.info("Solicitud PATCH /api/pedidos/{}/entregar", id);
        PedidoResponse response = pedidoService.marcarComoEntregado(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable UUID id) {
        log.info("Solicitud DELETE /api/pedidos/{}", id);
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> listarPedidos(
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Solicitud GET /api/pedidos - Listar pedidos");
        Page<PedidoResponse> response = pedidoService.listarPedidos(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorCliente(@PathVariable UUID clienteId) {
        log.info("Solicitud GET /api/pedidos/cliente/{}", clienteId);
        List<PedidoResponse> response = pedidoService.listarPedidosPorCliente(clienteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorEstado(@PathVariable EstadoPedido estado) {
        log.info("Solicitud GET /api/pedidos/estado/{}", estado);
        List<PedidoResponse> response = pedidoService.listarPedidosPorEstado(estado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        log.info("Solicitud GET /api/pedidos/fechas?fechaInicio={}&fechaFin={}", fechaInicio, fechaFin);
        List<PedidoResponse> response = pedidoService.listarPedidosPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<Page<PedidoResponse>> filtrarPedidos(
            @RequestBody PedidoFilter filtro,
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Solicitud POST /api/pedidos/filtrar");
        Page<PedidoResponse> response = pedidoService.filtrarPedidos(filtro, pageable);
        return ResponseEntity.ok(response);
    }
}
