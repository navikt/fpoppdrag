package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
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

    private static final MdcExtendedLogContext MDC_EXTENDED_LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    public PdpRequestBuilderImpl() {
        // CDI proxy
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<Long> behandlinger = dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID);
        Set<UUID> behandlingUUids = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        Set<String> saksnummer = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        setLogContext(behandlinger, behandlingUUids, saksnummer);

        return minimalbuilder()
            .medSaksnummer(saksnummer.stream().findFirst().orElse(null))
            .build();
    }

    @Override
    public AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        Set<Long> behandlinger = dataAttributter.getVerdier(AppAbacAttributtType.BEHANDLING_ID);
        Set<UUID> behandlingUUids = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        Set<String> saksnummer = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        setLogContext(behandlinger, behandlingUUids, saksnummer);
        return minimalbuilder().build();
    }

    private static void setLogContext(Set<Long> behandlinger, Set<UUID> behandlingUUids, Set<String> saksnummer) {
        behandlinger.stream().findFirst().ifPresent(behId -> {
            MDC_EXTENDED_LOG_CONTEXT.remove("behandlingId");
            MDC_EXTENDED_LOG_CONTEXT.add("behandlingId", behId);
        });
        behandlingUUids.stream().findFirst().ifPresent(uuid -> {
            MDC_EXTENDED_LOG_CONTEXT.remove("behandling");
            MDC_EXTENDED_LOG_CONTEXT.add("behandling", uuid.toString());
        });
        saksnummer.stream().findFirst().ifPresent(s -> {
            MDC_EXTENDED_LOG_CONTEXT.remove("saksnummer");
            MDC_EXTENDED_LOG_CONTEXT.add("saksnummer", s);
        });
    }

    private AppRessursData.Builder minimalbuilder() {
        return AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
    }
}
