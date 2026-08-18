package mx.edomex.rutas.tracking.controller;

import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.service.UbicacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UbicacionController.class)
class UbicacionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UbicacionService ubicacionService;

    @Test
    @WithMockUser
    void postUbicacion_returnsCreated() throws Exception {
        Ubicacion u = new Ubicacion();
        u.setId(1L);
        u.setLatitud(1.0);
        u.setLongitud(2.0);
        u.setFechaHora(LocalDateTime.now());

        when(ubicacionService.registrarUbicacion(eq(1L), any())).thenReturn(u);

        String body = "{\"latitud\":1.0,\"longitud\":2.0}";

        mockMvc.perform(post("/api/repartidores/1/ubicaciones")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitud").value(1.0));
    }

    @Test
    @WithMockUser
    void getHistorial_returnsList() throws Exception {
        Ubicacion u = new Ubicacion();
        u.setId(2L);
        u.setLatitud(3.0);
        u.setLongitud(4.0);
        u.setFechaHora(LocalDateTime.now());

        when(ubicacionService.obtenerHistorialPorRepartidor(1L)).thenReturn(List.of(u));

        when(ubicacionService.obtenerHistorialPorRepartidor(1L)).thenReturn(List.of(u));

        mockMvc.perform(get("/api/repartidores/1/ubicaciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }
}
