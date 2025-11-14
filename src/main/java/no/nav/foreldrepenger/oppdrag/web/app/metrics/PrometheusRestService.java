package no.nav.foreldrepenger.oppdrag.web.app.metrics;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import no.nav.vedtak.log.metrics.MetricsUtil;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    @Produces(TEXT_PLAIN)
    public String prometheus() {
        return MetricsUtil.scrape();
    }
}

