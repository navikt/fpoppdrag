package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.log.mdc.LoggFelter;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@Dependent
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    public PdpRequestBuilderImpl() {
        // CDI proxy
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        // Lag egen implementasjon for lagAppRessursDataForSystembruker dersom man leter fram id fra grunnlag her
        Set<Long> behandlinger = dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID);
        Set<UUID> behandlingUUids = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        Set<String> saksnummer = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);

        var builder = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
        saksnummer.stream().findFirst().ifPresent(builder::medSaksnummer);
        saksnummer.stream().findFirst().ifPresent(builder::medLoggSaksnummer);
        behandlingUUids.stream().findFirst().ifPresent(builder::medLoggBehandling);
        behandlinger.stream().findFirst().ifPresent(b -> builder.medLoggFelt(LoggFelter.BEHANDLING_ID, String.valueOf(b)));
        return builder.build();
    }
}
