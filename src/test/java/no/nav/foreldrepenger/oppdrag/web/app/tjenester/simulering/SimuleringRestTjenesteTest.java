package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.request.SimuleringResultatRequest;
import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;

@ExtendWith(JpaExtension.class)
class SimuleringRestTjenesteTest {

    private SimuleringRestTjeneste restTjeneste;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        var simuleringRepository = new SimuleringRepository(entityManager);
        var simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
        var hentNavnTjeneste = mock(PersonTjeneste.class);
        var simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository,
                hentNavnTjeneste, simuleringBeregningTjeneste);
        restTjeneste = new SimuleringRestTjeneste(simuleringResultatTjeneste, null);
    }

    @Test
    void returnererNullDersomSimuleringForBehandlingIkkeFinnes() {
        var bUuid = UUID.randomUUID();
        var sak = "123456789";
        var simuleringDto = restTjeneste.hentSimuleringResultatMedOgUtenInntrekk(
                new SimuleringResultatRequest(12345L, bUuid, sak));
        assertThat(simuleringDto).isNull();

        var simuleringResultatDto = restTjeneste.hentSimuleringResultat(new SimuleringResultatRequest(12345L, bUuid, sak));
        assertThat(simuleringResultatDto).isNull();
    }

}
