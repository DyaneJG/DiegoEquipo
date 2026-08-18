package mx.edomex.rutas.tracking.service;

import mx.edomex.rutas.repartidores.model.Repartidor;
import mx.edomex.rutas.repartidores.repository.RepartidorRepository;
import mx.edomex.rutas.tracking.model.Ubicacion;
import mx.edomex.rutas.tracking.repository.UbicacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceImplTest {

    @Mock
    UbicacionRepository ubicacionRepository;

    @Mock
    RepartidorRepository repartidorRepository;

    @InjectMocks
    UbicacionServiceImpl ubicacionService;

    @Test
    void registrarUbicacion_setsRepartidorAndFechaHora_andSaves() {
        Repartidor r = new Repartidor();
        r.setId(1L);

        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(r));

        Ubicacion u = new Ubicacion();
        u.setLatitud(10.0);
        u.setLongitud(20.0);

        when(ubicacionRepository.save(any(Ubicacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Ubicacion saved = ubicacionService.registrarUbicacion(1L, u);

        assertThat(saved.getRepartidor()).isNotNull();
        assertThat(saved.getRepartidor().getId()).isEqualTo(1L);
        assertThat(saved.getFechaHora()).isNotNull();
        verify(ubicacionRepository, times(1)).save(saved);
    }

    @Test
    void obtenerHistorialPorRepartidor_delegatesToRepository() {
        Repartidor r = new Repartidor();
        r.setId(2L);
        when(repartidorRepository.findById(2L)).thenReturn(Optional.of(r));

        Ubicacion u1 = new Ubicacion();
        u1.setId(1L);
        u1.setFechaHora(LocalDateTime.now());

        when(ubicacionRepository.findByRepartidorIdOrderByFechaHoraDesc(2L)).thenReturn(List.of(u1));

        List<Ubicacion> result = ubicacionService.obtenerHistorialPorRepartidor(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(ubicacionRepository, times(1)).findByRepartidorIdOrderByFechaHoraDesc(2L);
    }
}
