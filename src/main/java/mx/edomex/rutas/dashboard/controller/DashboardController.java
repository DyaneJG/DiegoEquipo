package mx.edomex.rutas.dashboard.controller;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;
import mx.edomex.rutas.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public DashboardResumenResponse getResumen() {
        return dashboardService.getResumenGeneral();
    }

    @GetMapping("/ventas")
    public List<VentasPorPeriodoResponse> getVentasPorPeriodo(
        @RequestParam(defaultValue = "mes") String periodo,
        @RequestParam(defaultValue = "2") Integer meses
    ) {
        return dashboardService.getVentasPorPeriodo(periodo, meses);
    }
}
