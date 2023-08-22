package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip;


import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;

@ExtendWith(JpaExtension.class)
class PipRepositoryTest {

    private PipRepository pipRepository;
    private SimuleringRepository simuleringRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        pipRepository = new PipRepository(entityManager);
        simuleringRepository = new SimuleringRepository(entityManager);
    }

    @Test
    void henterAktørIdForBehandlingId() {
        var aktørId = "44556677";
        var behandlingId = 234L;
        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medSimuleringResultat(SimuleringResultat.builder().build())
                .medAktørId(aktørId)
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medYtelseType(YtelseType.FP)
                .build();
        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        var aktørIdForBehandling = pipRepository.getAktørIdForBehandling(behandlingId);
        assertThat(aktørIdForBehandling).isPresent();
        assertThat(aktørIdForBehandling.get()).contains(aktørId);
    }

    @Test
    void returnererOptionalEmptyHvisGrunnlagForBehandlingIkkeFinnes() {
        var aktørIdForBehandling = pipRepository.getAktørIdForBehandling(123L);
        assertThat(aktørIdForBehandling).isNotPresent();
    }

}
