package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdpRequestBuilderImpl.class);


    private PipRepository pipRepository;

    public PdpRequestBuilderImpl() {
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        sjekkAttributter(attributter);

        var aktørIder = new HashSet<>(attributter.getAktørIder());
        if (!attributter.getBehandlingsIder().isEmpty()) {
            Long behandlingId = attributter.getBehandlingsIder().iterator().next();
            LOGGER.debug("Slår opp aktørId for behandling {}", behandlingId);
            Optional<String> aktørId = pipRepository.getAktørIdForBehandling(behandlingId);
            aktørId.ifPresent((a) -> {
                aktørIder.add(a);
                LOGGER.debug("Fant aktørId for behandling " + behandlingId);
            });
        }

        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.setToken(attributter.getIdToken());
        pdpRequest.setAction(attributter.getActionType());
        pdpRequest.setResource(attributter.getResource());
        pdpRequest.setAktørId(aktørIder);
        return pdpRequest;
    }

    private static void sjekkAttributter(AbacAttributtSamling attributter) {
        sjekkMaksEnBehandlingId(attributter.getBehandlingsIder());

        sjekkAtTom(attributter.getOppgaveIder(), "oppgaverId");
        sjekkAtTom(attributter.getFnrForSøkEtterSaker(), "saker med FNR");
        sjekkAtTom(attributter.getDokumentIDer(), "dokumentId");
        sjekkAtTom(attributter.getFagsakIder(), "fagsakId");
        sjekkAtTom(attributter.getAksjonspunktKode(), "aksjonpunktKode");
        sjekkAtTom(attributter.getDokumentDataIDer(), "dokumentDataID");
        sjekkAtTom(attributter.getDokumentforsendelseIder(), "dokumentForsendelseId");
        sjekkAtTom(attributter.getJournalpostIder(true), "journalpostId");
        sjekkAtTom(attributter.getJournalpostIder(false), "journalpostId");
        sjekkAtTom(attributter.getSaksnummre(), "saksnumer");
        sjekkAtTom(attributter.getSPBeregningsIder(), "spbergningsId");
        sjekkAtTom(attributter.getFødselsnumre(), "fødselsnumre");
    }

    private static void sjekkMaksEnBehandlingId(Set<Long> behandlingsIder) {
        if (behandlingsIder != null && behandlingsIder.size() > 1) {
            throw PdpRequestBuilderFeil.FACTORY.ugyldigInputFlereBehandlingIder(behandlingsIder).toException();
        }
    }

    private static <T> void sjekkAtTom(Collection<T> collection, String param) {
        if (collection != null && !collection.isEmpty()) {
            throw new IllegalArgumentException("Utvikler-feil: Attributten " + param + " er ikke støttet p.t. Bruker du riktig attributt? Hvis ja, legg til støtte");
        }
    }
}