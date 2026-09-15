package mx.edomex.rutas.dashboard.service;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;

import java.util.List;

public interface DashboardService {

    DashboardResumenResponse getResumenGeneral();

    List<VentasPorPeriodoResponse> getVentasPorPeriodo(String periodo, Integer meses);
}
