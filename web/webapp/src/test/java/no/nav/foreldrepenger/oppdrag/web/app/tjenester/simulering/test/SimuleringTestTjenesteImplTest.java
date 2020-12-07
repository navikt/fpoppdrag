package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;

@CdiDbAwareTest
public class SimuleringTestTjenesteImplTest {

    @Inject
    private SimuleringTestTjeneste simuleringTestTjeneste;
    @Inject
    private EntityManager entityManager;

    @Test
    public void skal_lagreSimuleringTestData_medGyldigInput() {
        // Arrange
        SimuleringDetaljerDto simuleringDetaljerDto = new SimuleringDetaljerDto(LocalDate.now(), LocalDate.now(), FagOmrådeKode.FORELDREPENGER.getKode(),
                new BigDecimal("100.00"), BetalingType.DEBIT.getKode(), PosteringType.FEILUTBETALING.getKode(),
                LocalDate.now().plusDays(14), false);

        SimuleringMottakerDto simuleringMottakerDto = new SimuleringMottakerDto("213242", MottakerType.BRUKER.getKode(), Lists.newArrayList(simuleringDetaljerDto));
        SimuleringDto simuleringDto = new SimuleringDto(123L, "0", Lists.newArrayList(simuleringMottakerDto));
        SimuleringGjelderDto simuleringGjelderDto = new SimuleringGjelderDto(Lists.newArrayList(simuleringDto));

        // Act
        simuleringTestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);

        // Assert
        List<SimuleringResultat> simuleringResultatData = hentAlle();
        assertThat(simuleringResultatData.size()).isGreaterThan(0);
    }

    private List<SimuleringResultat> hentAlle() {
        CriteriaQuery<SimuleringResultat> criteria = entityManager.getCriteriaBuilder().createQuery(SimuleringResultat.class);
        criteria.select(criteria.from(SimuleringResultat.class));
        return entityManager.createQuery(criteria).getResultList();
    }
}
