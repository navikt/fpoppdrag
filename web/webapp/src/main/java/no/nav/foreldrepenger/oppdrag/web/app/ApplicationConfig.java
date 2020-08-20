package no.nav.foreldrepenger.oppdrag.web.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.FellesKlasserForRest;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.SimuleringVedlikeholdRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.SimuleringTestRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

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
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(OpenApiResource.class);
        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.addAll(FellesKlasserForRest.getClasses());

        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(SimuleringRestTjeneste.class);

        classes.add(SimuleringVedlikeholdRestTjeneste.class);

        //HAXX SimuleringTestRestTjeneste skal bare være tilgjengelig for lokal utvikling, brukes for å sette opp test
        //hvis denne legges til i en egen Application isdf i denne, kan man ikke bruke swagger for å nå tjenesten
        //bruker derfor CDI for å slå opp klassen
        Instance<SimuleringTestRestTjeneste> simuleringTestRestTjeneste = CDI.current().select(SimuleringTestRestTjeneste.class);
        if (!simuleringTestRestTjeneste.isUnsatisfied()) {
            TargetInstanceProxy proxy = (TargetInstanceProxy) simuleringTestRestTjeneste.get();
            classes.add(proxy.weld_getTargetClass());
        }

        return Collections.unmodifiableSet(classes);
    }
}
