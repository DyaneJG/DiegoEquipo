package mx.edomex.rutas.dashboard.dto;

/**
 * DTO para graficar ventas por periodo.
 *
 * Se usa para construir series de tiempo en gráficas de ventas,
 * por ejemplo: diario, semanal o mensual.
 */
public record VentasPorPeriodoResponse(
    String etiqueta,
    Double total,
    Integer cantidad,
    Double variacionPorcentual
) {}
