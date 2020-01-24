package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;

public class SimuleringTestRestTjenesteTest {

    private static final String KONTO = "100000";
    private static final BigDecimal BELØP = BigDecimal.valueOf(100.00);
    private static final String DEBIT_TYPE = BetalingType.DEBIT.getKode();
    private static final String POSTERINGTYPE = PosteringType.YTELSE.getKode();
    private static final String KLASSEKODE = KlasseKode.FPATORD.getKode();
    private static LocalDate FOM = LocalDate.of(2018, 11, 1);
    private static LocalDate TOM = LocalDate.of(2018, 11, 30);
    private static LocalDate FORFALL = TOM.plusDays(14);
    private static String FAGOMRÅDEKODE = FagOmrådeKode.FORELDREPENGER.getKode();
    private SimuleringTestTjeneste simuleringTestTjeneste;

    private SimuleringTestRestTjeneste simuleringRestTjeneste;
    private FakeUnleash fakeUnleash = new FakeUnleash();

    @Before
    public void setUp() {
        simuleringTestTjeneste = Mockito.mock(SimuleringTestTjeneste.class);
        simuleringRestTjeneste = new SimuleringTestRestTjeneste(simuleringTestTjeneste, fakeUnleash);
        Mockito.doNothing().when(simuleringTestTjeneste).lagreSimuleringTestData(Mockito.any(SimuleringGjelderDto.class));
    }

    @Test
    public void skal_lagreSimuleringTestData_med_gyldig_data() {
        fakeUnleash.enable("fpoppdrag.testgrensesnitt");
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, KONTO, BELØP, DEBIT_TYPE, POSTERINGTYPE, KLASSEKODE, FORFALL, false, true);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));

        Response response = simuleringRestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
    public void skal_ikke_gjøre_noe_og_returnere_404_når_feature_er_skrudd_av() {
        fakeUnleash.disableAll();

        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, KONTO, BELØP, DEBIT_TYPE, POSTERINGTYPE, KLASSEKODE, FORFALL, false, true);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));

        Response response = simuleringRestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);

        Mockito.verifyZeroInteractions(simuleringTestTjeneste);
    }
}