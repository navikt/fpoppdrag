package no.nav.foreldrepenger.oppdrag.domenetjenester.person;

import java.util.Optional;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.Personinfo;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

public interface TpsTjeneste {

    Optional<AktørId> hentAktørForFnr(PersonIdent fnr);

    Optional<PersonIdent> hentFnr(AktørId aktørId);

    Optional<Personinfo> hentPersoninfoForAktør(AktørId aktørId);
}
