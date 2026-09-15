package mx.edomex.rutas.dashboard.service;

import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;

import java.util.List;

/**
 * Contrato del servicio del dashboard.
 *
 * La idea es separar la lógica de consulta del detalle del controlador.
 * Este servicio es el punto de entrada para todas las métricas del sistema.
 */
public interface DashboardService {

    /**
     * Devuelve las métricas generales para el panel principal.
     */
    DashboardResumenResponse getResumenGeneral();

    /**
     * Devuelve la evolución de ventas según el periodo solicitado.
     */
    List<VentasPorPeriodoResponse> getVentasPorPeriodo(String periodo, Integer meses);
}
