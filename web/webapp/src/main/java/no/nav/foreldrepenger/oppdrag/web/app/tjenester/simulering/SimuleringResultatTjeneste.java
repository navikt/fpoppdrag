package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import java.util.Optional;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;

public interface SimuleringResultatTjeneste {

    Optional<SimuleringResultatDto> hentResultatFraSimulering(Long behandlingId);

    Optional<SimuleringDto> hentDetaljertSimuleringsResultat(Long behandlingId);

    FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long behandlingId);
}
