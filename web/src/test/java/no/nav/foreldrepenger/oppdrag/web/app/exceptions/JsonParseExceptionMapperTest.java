package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.io.JsonEOFException;

class JsonParseExceptionMapperTest {

    @Test
    void skal_parse_JsonEOFException() {
        var mapper = new JsonParseExceptionMapper();
        var feilTekst = "Ukjent EOF";
        var resultat = mapper.toResponse(new JsonEOFException(null, null, feilTekst));
        var dto = (FeilDto) resultat.getEntity();
        assertThat(dto.feilmelding()).contains("JSON-parsing feil: " + feilTekst);
        assertThat(dto.feltFeil()).isEmpty();
    }
}
