package no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsAdapter;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

@ApplicationScoped
public class TpsAdapterImpl implements TpsAdapter {

    private AktørConsumerMedCache aktørConsumer;
    private PersonConsumer personConsumer;

    public TpsAdapterImpl() {
    }

    @Inject
    public TpsAdapterImpl(AktørConsumerMedCache aktørConsumer,
                          PersonConsumer personConsumer) {
        this.aktørConsumer = aktørConsumer;
        this.personConsumer = personConsumer;
    }

    @Override
    public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent) {
        return aktørConsumer.hentAktørIdForPersonIdent(personIdent.getIdent()).map(AktørId::new);
    }

    @Override
    public Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId) {
        return aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new);
    }

    @Override
    public Personinfo hentPersoninfo(String fnr) {
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(TpsUtil.lagPersonIdent(fnr));
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            Person person = response.getPerson();
            if (!(person instanceof Bruker)) {
                throw TpsFeilmeldinger.FACTORY.ukjentBrukerType().toException();
            }
            Bruker bruker = (Bruker) person;
            return new Personinfo(new PersonIdent(fnr), bruker.getPersonnavn().getSammensattNavn());
        } catch (HentPersonPersonIkkeFunnet e) {
            throw TpsFeilmeldinger.FACTORY.fantIkkePerson(e).toException();
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw TpsFeilmeldinger.FACTORY.tpsUtilgjengeligSikkerhetsbegrensning(e).toException();
        }
    }
}
