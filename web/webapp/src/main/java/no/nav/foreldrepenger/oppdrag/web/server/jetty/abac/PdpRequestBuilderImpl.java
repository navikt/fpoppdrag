package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.abac.xacml.NavAttributter;
import no.nav.abac.xacml.StandardAttributter;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "foreldrepenger";

    private static final Set<AbacAttributtType> STØTTEDE_TYPER = Set.of(
            StandardAbacAttributtType.BEHANDLING_ID,
            StandardAbacAttributtType.AKTØR_ID
    );

    private PipRepository pipRepository;

    PdpRequestBuilderImpl() {
        //for CDI proxy
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        validerTyper(attributter);

        var aktørIder = new HashSet<>(attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID));
        Optional<Long> behandlingIdOpt = utledBehandlingId(attributter);
        if (behandlingIdOpt.isPresent()) {
            Long behandlingId = behandlingIdOpt.get();
            Optional<String> aktørId = pipRepository.getAktørIdForBehandling(behandlingId);
            aktørId.ifPresent(aktørIder::add);
        }

        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(StandardAttributter.ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
        pdpRequest.put(NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørIder);

        return pdpRequest;
    }

    private void validerTyper(AbacAttributtSamling attributter) {
        for (AbacAttributtType type : attributter.keySet()) {
            if (!STØTTEDE_TYPER.contains(type)) {
                throw new IllegalArgumentException("Utvikler-feil: ikke-implementert støtte for minst en av typene: " + attributter.keySet());
            }
        }
    }

    private Optional<Long> utledBehandlingId(AbacAttributtSamling attributter) {
        Collection<?> behandlingIder = attributter.getVerdier(StandardAbacAttributtType.BEHANDLING_ID);
        if (behandlingIder.isEmpty()) {
            return Optional.empty();
        } else if (behandlingIder.size() == 1) {
            return Optional.of((Long) behandlingIder.iterator().next());
        }
        throw PdpRequestBuilderFeil.FACTORY.ugyldigInputFlereBehandlingIder(behandlingIder).toException();
    }
}