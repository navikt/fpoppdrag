package no.nav.foreldrepenger.oppdrag.domenetjenester.person;

import java.util.Optional;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.Personinfo;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;


public interface TpsAdapter {

    Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent);

    Personinfo hentPersoninfo(String fnr);

    Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId);
}
