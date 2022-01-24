package no.nav.foreldrepenger.oppdrag.web.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.SimuleringVedlikeholdRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.SimuleringTestRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

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
//        SecurityScheme ssApiKey = new SecurityScheme();
//        ssApiKey.in(SecurityScheme.In.HEADER).type(SecurityScheme.Type.APIKEY).name("apiKeyAuth").scheme("bearer");
//
//        SecurityScheme openIdConnect = new SecurityScheme();
//        openIdConnect.type(SecurityScheme.Type.OPENIDCONNECT).openIdConnectUrl("https://isso-q.adeo.no:443/isso/oauth2").name("openIdConnect");
//
//
//        oas.addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"));
//        oas.addSecurityItem(new SecurityRequirement().addList("openIdConnect"));

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
                .resourcePackages(Stream.of("no.nav")
                        .collect(Collectors.toSet()));

//        oas.getComponents().addSecuritySchemes("test", openIdConnect);
        try {
            new JaxrsOpenApiContextBuilder<>()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // eksponert grensesnitt
        classes.add(KodeverkRestTjeneste.class);
        classes.add(SimuleringRestTjeneste.class);
        classes.add(SimuleringVedlikeholdRestTjeneste.class);

        if (ER_LOKAL_UTVIKLING) {
            classes.add(SimuleringTestRestTjeneste.class);
        }

        // swagger
        classes.add(OpenApiResource.class);

        // Applikasjonsoppsett
        classes.add(JacksonJsonConfig.class);

        // ExceptionMappers pga de som finnes i Jackson+Jersey-media
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);

        // Generell exceptionmapper m/logging for øvrige tilfelle
        classes.add(GeneralRestExceptionMapper.class);

        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}