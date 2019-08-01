package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepositoryImpl;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class PipRepositoryTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private PipRepository pipRepository = new PipRepository(repoRule.getEntityManager());
    private SimuleringRepository simuleringRepository = new SimuleringRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void henterAktørIdForBehandlingId() {
        String aktørId = "44556677";
        long behandlingId = 234L;
        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medSimuleringResultat(SimuleringResultat.builder().build())
                .medAktørId(aktørId)
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medYtelseType(YtelseType.FORELDREPENGER)
                .build();
        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        Optional<String> aktørIdForBehandling = pipRepository.getAktørIdForBehandling(behandlingId);
        assertThat(aktørIdForBehandling).isPresent();
        assertThat(aktørIdForBehandling.get()).isEqualTo(aktørId);
    }

    @Test
    public void returnererOptionalEmptyHvisGrunnlagForBehandlingIkkeFinnes() {
        Optional<String> aktørIdForBehandling = pipRepository.getAktørIdForBehandling(123L);
        assertThat(aktørIdForBehandling).isNotPresent();
    }

}