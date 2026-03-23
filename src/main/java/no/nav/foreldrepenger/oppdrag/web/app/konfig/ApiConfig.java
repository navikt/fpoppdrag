package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ServerProperties;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.OppdragNedetidExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;
import no.nav.vedtak.server.rest.GeneralRestExceptionMapper;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";

    public ApiConfig() {
        GeneralRestExceptionMapper.setBrukerRettetApplikasjon(false);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // eksponert grensesnitt
        classes.add(SimuleringRestTjeneste.class);

        classes.add(FpRestJackson2Feature.class);
        classes.add(OppdragNedetidExceptionMapper.class);

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
