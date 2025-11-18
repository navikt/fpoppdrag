package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ServerProperties;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";


    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // eksponert grensesnitt
        classes.add(SimuleringRestTjeneste.class);

        // Autentisering
        classes.add(AuthenticationFilter.class);

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
