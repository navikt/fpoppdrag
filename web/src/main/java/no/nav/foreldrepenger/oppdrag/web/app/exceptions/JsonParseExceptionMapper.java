package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger log = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException exception) {
        var melding = String.format("FPO-299955 JSON-parsing feil: %s", exception.getMessage());
        log.warn(melding, exception);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(melding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


}
