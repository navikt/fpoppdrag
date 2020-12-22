package no.nav.foreldrepenger.oppdrag;

import java.util.List;

import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;

public interface OppdragConsumer {

    SimulerBeregningResponse hentSimulerBeregningResponse(SimulerBeregningRequest simulerBeregningRequest, List<String> source);

}
