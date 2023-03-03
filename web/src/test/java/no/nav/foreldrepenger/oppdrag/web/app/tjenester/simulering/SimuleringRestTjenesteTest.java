package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;

@ExtendWith(JpaExtension.class)
class SimuleringRestTjenesteTest {

    private SimuleringRestTjeneste restTjeneste;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        var simuleringRepository = new SimuleringRepository(entityManager);
        var simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
        var hentNavnTjeneste = mock(HentNavnTjeneste.class);
        var simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository,
                hentNavnTjeneste, simuleringBeregningTjeneste);
        restTjeneste = new SimuleringRestTjeneste(simuleringResultatTjeneste, null);
    }

    @Test
    void returnererNullDersomSimuleringForBehandlingIkkeFinnes() {
        var simuleringDto = restTjeneste.hentSimuleringResultatMedOgUtenInntrekk(
                new BehandlingIdDto("12345"));
        assertThat(simuleringDto).isNull();

        var simuleringResultatDto = restTjeneste.hentSimuleringResultat(new BehandlingIdDto("12345"));
        assertThat(simuleringResultatDto).isNull();
    }

}
