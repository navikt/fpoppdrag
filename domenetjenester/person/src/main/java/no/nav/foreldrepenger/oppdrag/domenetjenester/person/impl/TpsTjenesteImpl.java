package no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsAdapter;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

@ApplicationScoped
public class TpsTjenesteImpl implements TpsTjeneste {

    private TpsAdapter tpsAdapter;

    public TpsTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public TpsTjenesteImpl(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    @Override
    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return tpsAdapter.hentAktørIdForPersonIdent(fnr);
    }

    @Override
    public Optional<PersonIdent> hentFnr(AktørId aktørId) {
        return tpsAdapter.hentIdentForAktørId(aktørId);
    }

    @Override
    public Optional<Personinfo> hentPersoninfoForAktør(AktørId aktørId) {
        Optional<PersonIdent> funnetFnr = hentFnr(aktørId);
        return funnetFnr.map(fnr -> tpsAdapter.hentPersoninfo(fnr.getIdent()));
    }
}
