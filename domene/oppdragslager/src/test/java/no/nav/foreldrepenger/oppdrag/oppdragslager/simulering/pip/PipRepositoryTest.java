package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.FPoppdragEntityManagerAwareExtension;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;

@ExtendWith(FPoppdragEntityManagerAwareExtension.class)
public class PipRepositoryTest {

    private PipRepository pipRepository;
    private SimuleringRepository simuleringRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        pipRepository = new PipRepository(entityManager);
        simuleringRepository = new SimuleringRepository(entityManager);
    }

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
