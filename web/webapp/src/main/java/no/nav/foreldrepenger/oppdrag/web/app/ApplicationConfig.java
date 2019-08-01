package no.nav.foreldrepenger.oppdrag.web.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.FellesKlasserForRest;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.SimuleringRestTjeneste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.SimuleringTestRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.isso.config.ServerInfo;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (ServerInfo.instance().isUsingTLS()) {
            beanConfig.setSchemes(new String[]{"https"});
        } else {
            beanConfig.setSchemes(new String[]{"http"});

        }
        beanConfig.setBasePath("/fpoppdrag/api");
        beanConfig.setResourcePackage("no.nav");
        beanConfig.setTitle("Foreldrepenger oppdrag - App Skeleton");
        beanConfig.setDescription("Jetty Java App m/sikkerhet, swagger, dokumentasjon, db, metrics, osv. for deployment til NAIS");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);


        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.addAll(FellesKlasserForRest.getClasses());

        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(SimuleringRestTjeneste.class);
        classes.add(SimuleringTestRestTjeneste.class);

        return Collections.unmodifiableSet(classes);
    }
}
