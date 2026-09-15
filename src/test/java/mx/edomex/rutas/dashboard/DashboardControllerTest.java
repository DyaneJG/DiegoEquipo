package mx.edomex.rutas.dashboard;

import mx.edomex.rutas.dashboard.controller.DashboardController;
import mx.edomex.rutas.dashboard.dto.DashboardResumenResponse;
import mx.edomex.rutas.dashboard.dto.VentasPorPeriodoResponse;
import mx.edomex.rutas.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de comportamiento del controller del dashboard.
 *
 * Esta validación no prueba la base de datos ni la lógica real de agregación,
 * sino que confirma que la API expone correctamente el contrato esperado
 * para el frontend y que el controller delega adecuadamente al servicio.
 */
class DashboardControllerTest {

    private MockMvc mockMvc;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = mock(DashboardService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();
    }

    @Test
    void shouldReturnResumenDashboard() throws Exception {
        DashboardResumenResponse resumen = new DashboardResumenResponse(
            125000.0,
            48,
            14,
            22,
            96.5
        );

        when(dashboardService.getResumenGeneral()).thenReturn(resumen);

        mockMvc.perform(get("/api/dashboard/resumen")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ventasTotales").value(125000.0))
            .andExpect(jsonPath("$.pedidosActivos").value(48))
            .andExpect(jsonPath("$.rutasEnCurso").value(14))
            .andExpect(jsonPath("$.repartidoresDisponibles").value(22))
            .andExpect(jsonPath("$.tasaCumplimiento").value(96.5));
    }

    @Test
    void shouldReturnVentasPorPeriodo() throws Exception {
        List<VentasPorPeriodoResponse> ventas = List.of(
            new VentasPorPeriodoResponse("Enero", 12000.0, 42, 8.5),
            new VentasPorPeriodoResponse("Febrero", 15000.0, 48, 12.5)
        );

        when(dashboardService.getVentasPorPeriodo("mes", 2)).thenReturn(ventas);

        mockMvc.perform(get("/api/dashboard/ventas")
                .param("periodo", "mes")
                .param("meses", "2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].etiqueta").value("Enero"))
            .andExpect(jsonPath("$[0].total").value(12000.0))
            .andExpect(jsonPath("$[0].cantidad").value(42))
            .andExpect(jsonPath("$[0].variacionPorcentual").value(8.5));
    }
}
