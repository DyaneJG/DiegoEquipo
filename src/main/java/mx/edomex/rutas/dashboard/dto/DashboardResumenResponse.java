package mx.edomex.rutas.dashboard.dto;

public record DashboardResumenResponse(
    Double ventasTotales,
    Integer pedidosActivos,
    Integer rutasEnCurso,
    Integer repartidoresDisponibles,
    Double tasaCumplimiento
) {}
