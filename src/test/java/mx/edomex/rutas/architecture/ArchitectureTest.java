package mx.edomex.rutas.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE = "mx.edomex.rutas";

    private static JavaClasses clases;

    private static final String[] DOMINIOS = {
        "auth", "clientes", "catalogo", "pedidos", "pagos",
        "rutas", "repartidores", "tracking", "notificaciones"
    };

    @BeforeAll
    static void importar() {
        clases = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);
    }

    @Test
    void ningun_dominio_debe_acceder_repository_de_otro_dominio() {
        for (String dominio : DOMINIOS) {
            String paquete = BASE + "." + dominio;
            ArchRule regla = noClasses()
                .that().resideOutsideOfPackage(paquete + "..")
                .should().accessClassesThat().resideInAPackage(paquete + ".repository..")
                .because("solo el propio dominio '%s' puede usar su repository".formatted(dominio))
                .allowEmptyShould(true);
            regla.check(clases);
        }
    }

    @Test
    void ningun_dominio_debe_acceder_model_de_otro_dominio() {
        for (String dominio : DOMINIOS) {
            String paquete = BASE + "." + dominio;
            ArchRule regla = noClasses()
                .that().resideOutsideOfPackage(paquete + "..")
                .should().accessClassesThat().resideInAPackage(paquete + ".model..")
                .because("solo el propio dominio '%s' puede usar sus entities".formatted(dominio))
                .allowEmptyShould(true);
            regla.check(clases);
        }
    }

    @Test
    void shared_no_debe_depender_de_ningun_dominio_de_negocio() {
        String[] paquetesDominios = new String[DOMINIOS.length];
        for (int i = 0; i < DOMINIOS.length; i++) {
            paquetesDominios[i] = BASE + "." + DOMINIOS[i] + "..";
        }
        ArchRule regla = noClasses()
            .that().resideInAPackage(BASE + ".shared..")
            .should().dependOnClassesThat().resideInAnyPackage(paquetesDominios)
            .because("shared es infraestructura transversal, nunca debe conocer un dominio de negocio");
        regla.check(clases);
    }
}