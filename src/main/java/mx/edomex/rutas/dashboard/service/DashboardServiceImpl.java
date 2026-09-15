package mx.edomex.rutas.dashboard.service;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de dashboard.
 *
 * En esta capa se concentra la lógica de agregación y cálculo de métricas.
 * Aquí es donde se combinarán datos de pedidos, pagos, rutas y repartidores
 * cuando se conecte a la base de datos real.
 *
 * Por ahora se usan datos de ejemplo para mantener la estructura preparada
 * antes de la integración final con persistencia y consultas SQL complejas.
 */
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
        // Se debe reemplazar por consultas agregadas reales según el dominio.
        // Ejemplos:
        // - pedidos + pagos para ventas
        // - fecha de entrega para periodización
        // - comparación de periodo actual vs anterior
        return List.of(
            new VentasPorPeriodoResponse("Enero", 12000.0, 42, 8.5),
            new VentasPorPeriodoResponse("Febrero", 15000.0, 48, 12.5)
        );
    }
}
