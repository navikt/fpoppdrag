package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger log = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        var melding = "FPO-252294 JSON-mapping feil";
        log.warn(melding, exception);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(melding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
