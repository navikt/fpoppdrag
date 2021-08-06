package no.nav.foreldrepenger.oppdrag.web.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.KnownExceptionMappers;
import no.nav.foreldrepenger.oppdrag.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.SimuleringVedlikeholdRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.SimuleringTestRestTjeneste;
import no.nav.vedtak.util.env.Environment;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends ResourceConfig {

    private static boolean ER_LOKAL_UTVIKLING = Environment.current().isLocal();

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Vedtaksløsningen - Oppdrag")
                .version("1.0")
                .description("REST grensesnitt for FpOppdrag.");

        oas.info(info)
                .addServersItem(new Server()
                        .url("/fpoppdrag"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
                .resourcePackages(Stream.of("no.nav")
                        .collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        register(OpenApiResource.class);
        register(JacksonJsonConfig.class);

        registerClasses(getApplicationClasses());

        registerInstances(new LinkedHashSet<>(new KnownExceptionMappers().getExceptionMappers()));

        property(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
    }

    private static Set<Class<?>> getApplicationClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(KodeverkRestTjeneste.class);
        classes.add(SimuleringRestTjeneste.class);
        classes.add(SimuleringVedlikeholdRestTjeneste.class);

        if (ER_LOKAL_UTVIKLING) {
            classes.add(SimuleringTestRestTjeneste.class);
        }
        return Collections.unmodifiableSet(classes);
    }
}
