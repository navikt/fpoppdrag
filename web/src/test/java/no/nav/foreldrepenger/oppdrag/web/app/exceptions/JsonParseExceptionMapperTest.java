package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.io.JsonEOFException;

public class JsonParseExceptionMapperTest {

    @Test
    public void skal_parse_JsonEOFException(){
        JsonParseExceptionMapper mapper = new JsonParseExceptionMapper();
        String feilTekst = "Ukjent EOF";
        Response resultat = mapper.toResponse(new JsonEOFException(null, null, feilTekst));
        FeilDto dto = (FeilDto) resultat.getEntity();
        assertThat(dto.feilmelding()).contains("JSON-parsing feil: " + feilTekst);
        assertThat(dto.feltFeil()).isEmpty();
    }
}
