package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.exception.IntegrasjonException;

class HentNavnTjenesteTest {

    private final PersonTjeneste tpsTjeneste = mock(PersonTjeneste.class);
    private final OrganisasjonTjeneste organisasjonTjeneste = mock(OrganisasjonTjeneste.class);
    private final HentNavnTjeneste hentNavnTjeneste = new HentNavnTjeneste(tpsTjeneste, organisasjonTjeneste);

    @Test
    void henterNavnGittFnr() {
        // Arrange
        var fnr = "12345678910";
        var aktørId = new AktørId("998877");
        var navn = "Dolly Duck";
        var personIdent = new PersonIdent(fnr);

        when(tpsTjeneste.hentAktørForFnr(personIdent)).thenReturn(Optional.of(aktørId));
        when(tpsTjeneste.hentNavnFor(personIdent)).thenReturn(Optional.of(navn));

        // Act
        var hentetNavn = hentNavnTjeneste.hentNavnGittFnr(fnr);

        // Assert
        assertThat(hentetNavn).isEqualTo(navn);
    }

    @Test
    void kasterFeilHvisAktørIkkeFinnes() {
        // Arrange
        var fnr = "12345678910";
        var personIdent = new PersonIdent(fnr);
        when(tpsTjeneste.hentAktørForFnr(personIdent)).thenReturn(Optional.empty());

        // Act
        assertThat(hentNavnTjeneste.hentNavnGittFnr(fnr)).isEqualTo("Ukjent navn");
    }

    @Test
    void kasterFeilHvisPersoninfoIkkeFinnes() {
        // Arrange
        var fnr = "12345678910";
        var aktørId = new AktørId("998877");
        var personIdent = new PersonIdent(fnr);

        when(tpsTjeneste.hentAktørForFnr(personIdent)).thenReturn(Optional.of(aktørId));
        when(tpsTjeneste.hentNavnFor(personIdent)).thenReturn(null);

        // Act
        assertThrows(Exception.class, () -> hentNavnTjeneste.hentNavnGittFnr(fnr));
    }

    @Test
    void henterNavnGittOrgnr() {
        // Arrange
        var orgnr = "999999999";
        var navn = "Arbeidsgiver";
        var organisasjonInfo = new OrganisasjonInfo(orgnr, navn);

        when(organisasjonTjeneste.hentOrganisasjonInfo(orgnr)).thenReturn(Optional.of(organisasjonInfo));

        // Act
        var hentetNavn = hentNavnTjeneste.hentNavnGittOrgnummer(orgnr);

        // Assert
        assertThat(hentetNavn).isEqualTo(navn);
    }

    @Test
    void kasterFeilHvisOrganisasjonsinfoIkkeFinnes() {
        // Arrange
        var orgnr = "999999999";

        when(organisasjonTjeneste.hentOrganisasjonInfo(orgnr)).thenReturn(Optional.empty());

        // Act
        assertThrows(IntegrasjonException.class, () -> hentNavnTjeneste.hentNavnGittOrgnummer(orgnr));
    }


}