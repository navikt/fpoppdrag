package no.nav.foreldrepenger.oppdrag.web.app.validering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testSkalPasserePåGyldigInput() {
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(simuleringGjelderDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testSkalFeilPåManglendeInput() {
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(new SimuleringGjelderDto(Lists.newArrayList()));
        assertEquals(1, violations.size());
        assertEquals("{javax.validation.constraints.Size.message}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    public void testSkalFeilPåUgyldigMottakerId() {
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, KREDIT_TYPE, POSTERINGTYPE, FORFALL, false);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto(RandomStringUtils.random(12), MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));
        Set<ConstraintViolation<SimuleringGjelderDto>> violations = validator.validate(simuleringGjelderDto);
        assertEquals(1, violations.size());
        assertEquals("ugyldig mottaker", violations.iterator().next().getMessageTemplate());
    }


}
