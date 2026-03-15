package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.OppdragNedetidException;
import no.nav.vedtak.feil.FeilDto;

class OppdragNedetidExceptionMapperTest {

    private final OppdragNedetidExceptionMapper exceptionMapper = new OppdragNedetidExceptionMapper();

    @Test
    void skalMappeOppdragNedetidTil503() {
        var response = exceptionMapper.toResponse(new OppdragNedetidException());

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        var feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.feiltype()).isEqualTo("OPPDRAG_FORVENTET_NEDETID");
    }

}
