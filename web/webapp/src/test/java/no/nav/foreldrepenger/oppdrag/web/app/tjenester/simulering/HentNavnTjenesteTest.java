package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.Personinfo;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.exception.IntegrasjonException;

public class HentNavnTjenesteTest {

    private TpsTjeneste tpsTjeneste = mock(TpsTjeneste.class);
    private OrganisasjonTjeneste organisasjonTjeneste = mock(OrganisasjonTjeneste.class);
    private HentNavnTjeneste hentNavnTjeneste = new HentNavnTjeneste(tpsTjeneste, organisasjonTjeneste);

    @Test
    public void henterNavnGittFnr() {
        // Arrange
        String fnr = "08069649719";
        AktørId aktørId = new AktørId("998877");
        String navn = "Dolly Duck";
        PersonIdent personIdent = new PersonIdent(fnr);
        Personinfo personinfo = new Personinfo(personIdent, navn);

        when(tpsTjeneste.hentAktørForFnr(eq(personIdent))).thenReturn(Optional.of(aktørId));
        when(tpsTjeneste.hentPersoninfoFor(eq(personIdent))).thenReturn(personinfo);

        // Act
        String hentetNavn = hentNavnTjeneste.hentNavnGittFnr(fnr);

        // Assert
        assertThat(hentetNavn).isEqualTo(navn);
    }

    @Test
    public void kasterFeilHvisAktørIkkeFinnes() {
        // Arrange
        String fnr = "08069649719";
        PersonIdent personIdent = new PersonIdent(fnr);
        when(tpsTjeneste.hentAktørForFnr(eq(personIdent))).thenReturn(Optional.empty());

        // Act
        assertThrows(Exception.class, () -> hentNavnTjeneste.hentNavnGittFnr(fnr));
    }

    @Test
    public void kasterFeilHvisPersoninfoIkkeFinnes() {
        // Arrange
        String fnr = "08069649719";
        AktørId aktørId = new AktørId("998877");
        PersonIdent personIdent = new PersonIdent(fnr);

        when(tpsTjeneste.hentAktørForFnr(eq(personIdent))).thenReturn(Optional.of(aktørId));
        when(tpsTjeneste.hentPersoninfoFor(eq(personIdent))).thenReturn(null);

        // Act
        assertThrows(Exception.class, () -> hentNavnTjeneste.hentNavnGittFnr(fnr));
    }

    @Test
    public void henterNavnGittOrgnr() {
        // Arrange
        String orgnr = "956321487";
        String navn = "Arbeidsgiver";
        OrganisasjonInfo organisasjonInfo = new OrganisasjonInfo(orgnr, navn);

        when(organisasjonTjeneste.hentOrganisasjonInfo(eq(orgnr))).thenReturn(Optional.of(organisasjonInfo));

        // Act
        String hentetNavn = hentNavnTjeneste.hentNavnGittOrgnummer(orgnr);

        // Assert
        assertThat(hentetNavn).isEqualTo(navn);
    }

    @Test
    public void kasterFeilHvisOrganisasjonsinfoIkkeFinnes() {
        // Arrange
        String orgnr = "956321487";

        when(organisasjonTjeneste.hentOrganisasjonInfo(eq(orgnr))).thenReturn(Optional.empty());

        // Act
        assertThrows(IntegrasjonException.class, () -> hentNavnTjeneste.hentNavnGittOrgnummer(orgnr));
    }


}