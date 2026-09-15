package mx.edomex.rutas.dashboard.controller;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;
import mx.edomex.rutas.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller del dashboard.
 *
 * Se encarga de exponer las métricas del sistema a nivel operativo.
 * La intención es mantener la capa web ligera: recibe la petición del frontend,
 * delega la lógica al servicio y devuelve DTOs preparados para el cliente.
 *
 * Importante: esta capa solo expone datos; no calcula reglas de negocio complejas.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Endpoint para mostrar el resumen ejecutivo del sistema.
     * Incluye métricas clave como ventas, pedidos activos, rutas y repartidores.
     */
    @GetMapping("/resumen")
    public DashboardResumenResponse getResumen() {
        return dashboardService.getResumenGeneral();
    }

    /**
     * Endpoint para consultar el comportamiento de ventas por periodo.
     *
     * @param periodo ejemplo: dia, semana, mes
     * @param meses cantidad de periodos a consultar
     * @return lista con totales y variación porcentual
     */
    @GetMapping("/ventas")
    public List<VentasPorPeriodoResponse> getVentasPorPeriodo(
        @RequestParam(defaultValue = "mes") String periodo,
        @RequestParam(defaultValue = "2") Integer meses
    ) {
        return dashboardService.getVentasPorPeriodo(periodo, meses);
    }
}
