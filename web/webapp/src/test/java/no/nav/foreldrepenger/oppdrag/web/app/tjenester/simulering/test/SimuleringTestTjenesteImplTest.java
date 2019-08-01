package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.kodeverk.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverk.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SimuleringTestTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private SimuleringTestTjeneste simuleringTestTjeneste;

    @Test
    public void skal_lagreSimuleringTestData_medGyldigInput() {
        // Arrange
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(LocalDate.now(), LocalDate.now(), FagOmrådeKode.FORELDREPENGER.getKode(), "100000",
                new BigDecimal("100.00"), BetalingType.DEBIT.getKode(), PosteringType.FEILUTBETALING.getKode(),
                KlasseKode.FPATFER.getKode(), LocalDate.now().plusDays(14), false, false);

        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));

        // Act
        simuleringTestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);

        // Assert
        List<SimuleringResultat> simuleringResultatData = repoRule.getRepository().hentAlle(SimuleringResultat.class);
        Assertions.assertThat(simuleringResultatData.size()).isGreaterThan(0);
    }
}