package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;

public class SimuleringTestRestTjenesteImplTest {

    private static final BigDecimal BELØP = BigDecimal.valueOf(100.00);
    private static final String DEBIT_TYPE = BetalingType.DEBIT.getKode();
    private static final String POSTERINGTYPE = PosteringType.YTELSE.getKode();
    private static LocalDate FOM = LocalDate.of(2018, 11, 1);
    private static LocalDate TOM = LocalDate.of(2018, 11, 30);
    private static LocalDate FORFALL = TOM.plusDays(14);
    private static String FAGOMRÅDEKODE = FagOmrådeKode.FORELDREPENGER.getKode();
    private SimuleringTestTjeneste simuleringTestTjeneste;

    private SimuleringTestRestTjenesteImpl simuleringRestTjeneste;

    @BeforeEach
    public void setUp() {
        simuleringTestTjeneste = Mockito.mock(SimuleringTestTjeneste.class);
        simuleringRestTjeneste = new SimuleringTestRestTjenesteImpl(simuleringTestTjeneste);
        Mockito.doNothing().when(simuleringTestTjeneste).lagreSimuleringTestData(Mockito.any(SimuleringGjelderDto.class));
    }

    @Test
    public void skal_lagreSimuleringTestData_med_gyldig_data() {
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(FOM, TOM, FAGOMRÅDEKODE, BELØP, DEBIT_TYPE, POSTERINGTYPE, FORFALL, false);
        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));

        Response response = simuleringRestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
    }

}
