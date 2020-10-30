package no.nav.foreldrepenger.oppdrag.domenetjenester.person;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class TpsTjenesteImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PersonConsumer personConsumer = Mockito.mock(PersonConsumer.class);
    private AktørConsumerMedCache aktørConsumer = Mockito.mock(AktørConsumerMedCache.class);

    private PdlKlient tpsAdapter = mock(PdlKlient.class);
    private TpsTjeneste tpsTjeneste = new TpsTjeneste(tpsAdapter, aktørConsumer, personConsumer);

    @Test
    public void finnerAktørIdForFnr() {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";

        when(aktørConsumer.hentAktørIdForPersonIdent(eq(fnr))).thenReturn(Optional.of(aktørId.getId()));

        // Act
        Optional<AktørId> funnetAktørId = tpsTjeneste.hentAktørForFnr(PersonIdent.fra(fnr));

        // Assert
        assertThat(funnetAktørId).isPresent();
        assertThat(funnetAktørId.get()).isEqualTo(aktørId);
    }

    @Test
    public void finnerFnrForAktørId() {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";
        when(aktørConsumer.hentPersonIdentForAktørId(eq(aktørId.getId()))).thenReturn(Optional.of(fnr));

        // Act
        Optional<PersonIdent> funnetPersonIdent = tpsTjeneste.hentFnr(aktørId);

        // Assert
        Assertions.assertThat(funnetPersonIdent).isPresent();
        assertThat(funnetPersonIdent.get().getIdent()).isEqualTo(fnr);
    }

    @Test
    public void finnerPersonInfoForAktørId() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";
        String navn = "Nasse Nøff";

        Bruker person = new Bruker();
        Personnavn personnavn = new Personnavn();
        personnavn.setSammensattNavn(navn);
        person.setPersonnavn(personnavn);
        HentPersonResponse response = new HentPersonResponse();
        response.setPerson(person);

        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(TpsUtil.lagPersonIdent(fnr));

        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class))).thenReturn(response);
        when(aktørConsumer.hentPersonIdentForAktørId(eq(aktørId.getId()))).thenReturn(Optional.of(fnr));

        // Act
        Optional<Personinfo> personinfo = tpsTjeneste.hentPersoninfoForAktør(aktørId);

        // Assert
        Assertions.assertThat(personinfo).isPresent();
        assertThat(personinfo.get().getPersonIdent()).isEqualTo(PersonIdent.fra(fnr));
        assertThat(personinfo.get().getNavn()).isEqualTo(navn);
    }

    @Test
    public void kasterExceptionDersomPersonIkkeFinnes() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";

        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(TpsUtil.lagPersonIdent(fnr));

        when(aktørConsumer.hentPersonIdentForAktørId(eq(aktørId.getId()))).thenReturn(Optional.of(fnr));
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet("", new PersonIkkeFunnet()));

        expectedException.expect(TekniskException.class);

        // Act
        tpsTjeneste.hentPersoninfoForAktør(aktørId);
    }

    @Test
    public void kasterExceptionDersomSikkerhetsbegrensningPåPerson() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";

        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(TpsUtil.lagPersonIdent(fnr));

        when(aktørConsumer.hentPersonIdentForAktørId(eq(aktørId.getId()))).thenReturn(Optional.of(fnr));
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning("", new Sikkerhetsbegrensning()));

        expectedException.expect(ManglerTilgangException.class);

        // Act
        tpsTjeneste.hentPersoninfoForAktør(aktørId);
    }


}