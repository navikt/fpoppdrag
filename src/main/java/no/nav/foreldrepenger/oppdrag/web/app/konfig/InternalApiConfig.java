package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.oppdrag.web.app.healthcheck.HealthCheckRestService;
import no.nav.foreldrepenger.oppdrag.web.app.metrics.PrometheusRestService;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends Application {

    public static final String API_URI = "/internal";

    public InternalApiConfig() {
        // CDI
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(HealthCheckRestService.class, PrometheusRestService.class);
    }

}
