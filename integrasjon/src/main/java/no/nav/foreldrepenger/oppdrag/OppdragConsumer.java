package no.nav.foreldrepenger.oppdrag;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;

public interface OppdragConsumer {

    SimulerBeregningResponse hentSimulerBeregningResponse(SimulerBeregningRequest simulerBeregningRequest) throws SimulerBeregningFeilUnderBehandling;

}
