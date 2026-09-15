package mx.edomex.rutas.dashboard.dto;

/**
 * DTO para el resumen ejecutivo del dashboard.
 *
 * Este payload responde a la vista principal del panel operativo.
 * Se usa para mostrar cifras clave de negocio en una sola respuesta.
 */
public record DashboardResumenResponse(
    Double ventasTotales,
    Integer pedidosActivos,
    Integer rutasEnCurso,
    Integer repartidoresDisponibles,
    Double tasaCumplimiento
) {}
