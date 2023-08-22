package no.nav.foreldrepenger.oppdrag.web.app.validering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;

class SimuleringResultatValidatorTest {

    private static final BigDecimal BELØP = BigDecimal.valueOf(100.00);
    private static final String KREDIT_TYPE = BetalingType.K.name();
    private static final String POSTERINGTYPE = PosteringType.JUST.name();
    private static final LocalDate FOM = LocalDate.of(2018, 11, 1);
    private static final LocalDate TOM = LocalDate.of(2018, 11, 30);
    private static final LocalDate FORFALL = TOM.plusDays(14);
    private static final String FAGOMRÅDEKODE = Fagområde.SP.name();
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSkalPasserePåGyldigInput() {
        var simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        var simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.name(), Lists.newArrayList(simuleringDetaljerDto));
        var simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        var simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        var violations = validator.validate(simuleringGjelderDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testSkalFeilPåManglendeInput() {
        var violations = validator.validate(new SimuleringGjelderDto(Lists.newArrayList()));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }

    @Test
    void testSkalFeilPåUgyldigMottakerId() {
        var simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        var simuleringMottakerDto = new SimuleringMottakerDto(RandomStringUtils.random(12), MottakerType.BRUKER.name(), Lists.newArrayList(simuleringDetaljerDto));
        var simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        var simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        var violations = validator.validate(simuleringGjelderDto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("ugyldig mottaker");
    }

}
