package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.OrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

public class OrganisasjonTjenesteTest {

    private OrganisasjonTjeneste organisasjonTjeneste;
    private OrganisasjonConsumer organisasjonConsumer;

    private static final String ORG_NUMMER = "973861778";

    @Before
    public void setUp() {
        organisasjonConsumer = mock(OrganisasjonConsumer.class);
        OrganisasjonAdapter organisasjonAdapter = new OrganisasjonAdapter(organisasjonConsumer);
        organisasjonTjeneste = new OrganisasjonTjeneste(organisasjonAdapter);
    }


    @Test
    public void skal_test_hentOrganisasjonInfo_medGyldigOrgnummer() throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        when(organisasjonConsumer.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(fylleUtHentOrganisasjonResponse());
        Optional<OrganisasjonInfo> organisasjonInfo = organisasjonTjeneste.hentOrganisasjonInfo(ORG_NUMMER);
        Assertions.assertThat(organisasjonInfo).isPresent();
        Assertions.assertThat(organisasjonInfo.get().getNavn()).isEqualToIgnoringCase("STATOIL ASA AVD STATOIL SOKKELVIRKSOMHET");
    }

    @Test
    public void skal_feil_test_hentOrganisasjonInfo_når_Orgikkefunnet() {
        try {
            when(organisasjonConsumer.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
                    .thenThrow(new HentOrganisasjonOrganisasjonIkkeFunnet("Organisasjon ikke funnet", new OrganisasjonIkkeFunnet()));
            organisasjonTjeneste.hentOrganisasjonInfo(ORG_NUMMER);
        } catch (Exception e) {
            Assertions.assertThat(e).isNotNull();
            Assertions.assertThat(e).isInstanceOf(TekniskException.class);
            TekniskException tekniskException = (TekniskException) e; //NOSONAR
            Assertions.assertThat(tekniskException.getFeil().getKode()).isEqualToIgnoringCase("FPO-9056718");
            Assertions.assertThat(tekniskException.getFeil().getFeilmelding()).isEqualToIgnoringCase("Fant ikke organisasjon");
        }
    }

    @Test
    public void skal_feil_test_hentOrganisasjonInfo_med_UgyldigOrgnummer() {
        try {
            when(organisasjonConsumer.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
                    .thenThrow(new HentOrganisasjonUgyldigInput("Orgnummerr er ikke gyldig", new UgyldigInput()));
            organisasjonTjeneste.hentOrganisasjonInfo("abc");
        } catch (Exception e) {
            Assertions.assertThat(e).isNotNull();
            Assertions.assertThat(e).isInstanceOf(TekniskException.class);
            TekniskException tekniskException = (TekniskException) e; //NOSONAR
            Assertions.assertThat(tekniskException.getFeil().getKode()).isEqualToIgnoringCase("FPO-9056719");
            Assertions.assertThat(tekniskException.getFeil().getFeilmelding()).isEqualToIgnoringCase("Ugyldig organisasjon kode");
        }
    }

    private HentOrganisasjonResponse fylleUtHentOrganisasjonResponse() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(ORG_NUMMER);
        UstrukturertNavn ustrukturertNavn = new UstrukturertNavn();
        ustrukturertNavn.getNavnelinje().add("STATOIL ASA AVD STATOIL SOKKELVIRKSOMHET");
        organisasjon.setNavn(ustrukturertNavn);
        response.setOrganisasjon(organisasjon);
        return response;
    }
}
