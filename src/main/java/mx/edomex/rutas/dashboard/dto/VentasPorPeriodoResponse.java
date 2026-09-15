package mx.edomex.rutas.dashboard.dto;

public record VentasPorPeriodoResponse(
    String etiqueta,
    Double total,
    Integer cantidad,
    Double variacionPorcentual
) {}
