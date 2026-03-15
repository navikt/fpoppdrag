package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.OppdragNedetidException;
import no.nav.vedtak.feil.FeilDto;

@Provider
public class OppdragNedetidExceptionMapper implements ExceptionMapper<OppdragNedetidException> {

    @Override
    public Response toResponse(OppdragNedetidException exception) {
        return Response.status(exception.getStatusCode())
            .entity(new FeilDto("OPPDRAG_FORVENTET_NEDETID", exception.getMessage()))
            .type(APPLICATION_JSON)
            .build();
    }

}
