package mx.edomex.rutas.dashboard.service;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardResumenResponse getResumenGeneral() {
        return new DashboardResumenResponse(
            125000.0,
            48,
            14,
            22,
            96.5
        );
    }

    @Override
    public List<VentasPorPeriodoResponse> getVentasPorPeriodo(String periodo, Integer meses) {
        return List.of(
            new VentasPorPeriodoResponse("Enero", 12000.0, 42, 8.5),
            new VentasPorPeriodoResponse("Febrero", 15000.0, 48, 12.5)
        );
    }
}
