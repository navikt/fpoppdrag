package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip.PipRepository;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final MdcExtendedLogContext MDC_EXTENDED_LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private PipRepository pipRepository;

    PdpRequestBuilderImpl() {
        //for CDI proxy
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<Long> behandlinger = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_ID);
        behandlinger.stream().findFirst().ifPresent(behId -> {
            MDC_EXTENDED_LOG_CONTEXT.remove("behandling");
            MDC_EXTENDED_LOG_CONTEXT.add("behandling", behId);
        });
        var aktørFraBehandling = behandlinger.stream()
                .map(b -> pipRepository.getAktørIdForBehandling(b))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        return AppRessursData.builder()
                .leggTilAktørIdSet(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
                .leggTilAktørIdSet(aktørFraBehandling)
                .build();
    }
}
