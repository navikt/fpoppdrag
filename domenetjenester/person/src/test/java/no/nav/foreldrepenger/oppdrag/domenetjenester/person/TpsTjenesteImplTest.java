package no.nav.foreldrepenger.oppdrag.domenetjenester.person;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.Identliste;
import no.nav.pdl.Navn;
import no.nav.pdl.Person;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.person.Persondata;

@ExtendWith(MockitoExtension.class)
public class TpsTjenesteImplTest {

    @Mock
    private Persondata pdlKlient;
    private PersonTjeneste tpsTjeneste;

    @BeforeEach
    public void setup() {
        tpsTjeneste = new PersonTjeneste(pdlKlient);
    }

    @Test
    public void finnerAktørIdForFnr() {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";

        when(pdlKlient.hentIdenter(any(), any())).thenReturn(new Identliste(List.of(new IdentInformasjon(aktørId.getId(), IdentGruppe.AKTORID, false))));
        ;

        // Act
        Optional<AktørId> funnetAktørId = tpsTjeneste.hentAktørForFnr(PersonIdent.fra(fnr));

        // Assert
        assertThat(funnetAktørId).isPresent();
        assertThat(funnetAktørId.get()).isEqualTo(aktørId);
    }

    @Test
    public void finnerPersonInfoForAktørId() {
        // Arrange
        String fnr = "24069305608";
        String navn = "Nasse Nøff";

        Person person = new Person();
        Navn navnPdl = new Navn("Nøff", null, "Nasse", "Nasse Nøff", null, null, null, null);
        person.setNavn(List.of(navnPdl));

        when(pdlKlient.hentPerson(any(), any())).thenReturn(person);

        // Act
        String personinfo = tpsTjeneste.hentNavnFor(new PersonIdent(fnr)).orElse(null);

        // Assert
        assertThat(personinfo).isEqualTo(navn);
    }

    @Test
    public void kasterExceptionPersonIkkeFunnetSomHåndteres() {
        // Arrange
        String fnr = "24069305608";
        var unntak = Mockito.mock(TekniskException.class);
        when(unntak.getKode()).thenReturn(PdlKlient.PDL_KLIENT_NOT_FOUND_KODE);


        when(pdlKlient.hentIdenter(any(), any())).thenThrow(unntak);

        // Act
        assertThat(tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr))).isEmpty();
    }

    @Test
    public void kasterExceptionSomIkkeHåndteresForAndreTilfelle() {
        // Arrange
        PersonIdent fnr = new PersonIdent("24069305608");
        var unntak = Mockito.mock(TekniskException.class);
        when(unntak.getKode()).thenReturn("Ukjent");


        when(pdlKlient.hentPerson(any(), any())).thenThrow(unntak);

        // Act
        assertThrows(TekniskException.class, () -> tpsTjeneste.hentNavnFor(fnr));
    }
}