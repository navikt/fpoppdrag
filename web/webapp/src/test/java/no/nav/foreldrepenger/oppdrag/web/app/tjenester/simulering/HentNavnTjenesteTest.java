package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.Personinfo;
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
        when(tpsTjeneste.hentPersoninfoForAktør(eq(aktørId))).thenReturn(Optional.of(personinfo));

        // Act
        String hentetNavn = hentNavnTjeneste.hentNavnGittFnr(fnr);

        // Assert
        assertThat(hentetNavn).isEqualTo(navn);
    }

    @Test(expected = IntegrasjonException.class)
    public void kasterFeilHvisAktørIkkeFinnes() {
        // Arrange
        String fnr = "08069649719";
        PersonIdent personIdent = new PersonIdent(fnr);
        when(tpsTjeneste.hentAktørForFnr(eq(personIdent))).thenReturn(Optional.empty());

        // Act
        hentNavnTjeneste.hentNavnGittFnr(fnr);
    }

    @Test(expected = IntegrasjonException.class)
    public void kasterFeilHvisPersoninfoIkkeFinnes() {
        // Arrange
        String fnr = "08069649719";
        AktørId aktørId = new AktørId("998877");
        PersonIdent personIdent = new PersonIdent(fnr);

        when(tpsTjeneste.hentAktørForFnr(eq(personIdent))).thenReturn(Optional.of(aktørId));
        when(tpsTjeneste.hentPersoninfoForAktør(eq(aktørId))).thenReturn(Optional.empty());

        // Act
        hentNavnTjeneste.hentNavnGittFnr(fnr);
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

    @Test(expected = IntegrasjonException.class)
    public void kasterFeilHvisOrganisasjonsinfoIkkeFinnes() {
        // Arrange
        String orgnr = "956321487";

        when(organisasjonTjeneste.hentOrganisasjonInfo(eq(orgnr))).thenReturn(Optional.empty());

        // Act
        hentNavnTjeneste.hentNavnGittOrgnummer(orgnr);
    }


}