package mx.edomex.rutas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Esta línea apaga la pantalla blanca de login para que puedas trabajar
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SistemaRmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaRmBackendApplication.class, args);
	}
}