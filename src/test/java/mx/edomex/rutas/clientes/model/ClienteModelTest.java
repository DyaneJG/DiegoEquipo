package mx.edomex.rutas.clientes.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteModelTest {

    @Test
    void debeCrearClienteYDireccionConValoresBasicos() {
        Cliente cliente = Cliente.builder()
                .nombreCompleto("Ana García")
                .correoElectronico("ana@example.com")
                .telefono("5512345678")
                .build();

        Direccion direccion = Direccion.builder()
                .cliente(cliente)
                .calle("Av. Siempre Viva")
                .numeroExterior("123")
                .colonia("Centro")
                .zona(ZonaEntrega.CHALCO)
                .codigoPostal("12345")
                .build();

        cliente.getDirecciones().add(direccion);

        assertThat(cliente.getActivo()).isTrue();
        assertThat(cliente.getDirecciones()).hasSize(1);
        assertThat(direccion.getCliente()).isSameAs(cliente);
        assertThat(direccion.getZona()).isEqualTo(ZonaEntrega.CHALCO);
    }
}
