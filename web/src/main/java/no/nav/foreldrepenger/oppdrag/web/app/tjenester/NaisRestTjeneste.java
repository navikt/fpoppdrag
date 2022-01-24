package no.nav.foreldrepenger.oppdrag.web.app.tjenester;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.oppdrag.web.app.selftest.Selftests;

@Path("/health")
@Produces(TEXT_PLAIN)
@ApplicationScoped
public class NaisRestTjeneste {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private ApplicationServiceStarter starterService;
    private Selftests selftests;

    private Boolean isContextStartupReady;

    public NaisRestTjeneste() {
        // CDI
    }

    @Inject
    public NaisRestTjeneste(ApplicationServiceStarter starterService, Selftests selftests) {
        this.starterService = starterService;
        this.selftests = selftests;
    }

    @GET
    @Path("isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (isContextStartupReady) { // Vurder hvilke tilfeller man ønsker restart
            return Response
                    .ok(RESPONSE_OK, MediaType.TEXT_PLAIN_TYPE)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        }
        return Response
                .serverError()
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
    }

    @GET
    @Path("isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        // Vurder hvilke tilfeller man ønsker oppskalering av antall noder
        if (isContextStartupReady && selftests.isReady()) {
            return Response
                    .ok(RESPONSE_OK, MediaType.TEXT_PLAIN_TYPE)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        }
        return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
    }

    @GET
    @Path("preStop")
    @Operation(description = "kalles på før stopp", tags = "nais", hidden = true)
    public Response preStop() {
        starterService.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }

    /**
     * Settes av AppstartupServletContextListener ved contextInitialized
     *
     * @param isContextStartupReady
     */
    public void setIsContextStartupReady(Boolean isContextStartupReady) {
        this.isContextStartupReady = isContextStartupReady;
    }

}
