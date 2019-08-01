package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.util.List;

public interface StartSimuleringTjeneste {

    void startSimulering(Long behandlingId, List<String> oppdragXml);

    void kansellerSimulering(Long behandlingId);

}
