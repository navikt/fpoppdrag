package no.nav.foreldrepenger.oppdrag.web.app.validering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;

public class SimuleringResultatValidatorTest {

    private static final BigDecimal BELØP = BigDecimal.valueOf(100.00);
    private static final String KREDIT_TYPE = BetalingType.KREDIT.getKode();
    private static final String POSTERINGTYPE = PosteringType.JUSTERING.getKode();
    private static LocalDate FOM = LocalDate.of(2018, 11, 1);
    private static LocalDate TOM = LocalDate.of(2018, 11, 30);
    private static LocalDate FORFALL = TOM.plusDays(14);
    private static String FAGOMRÅDEKODE = FagOmrådeKode.SYKEPENGER.getKode();
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testSkalPasserePåGyldigInput() {
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.name(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(simuleringGjelderDto);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testSkalFeilPåManglendeInput() {
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(new SimuleringGjelderDto(Lists.newArrayList()));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("{javax.validation.constraints.Size.message}");
    }

    @Test
    public void testSkalFeilPåUgyldigMottakerId() {
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto(RandomStringUtils.random(12), MottakerType.BRUKER.name(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(simuleringGjelderDto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessageTemplate()).isEqualTo("ugyldig mottaker");
    }


}
