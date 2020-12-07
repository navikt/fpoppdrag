package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonEReg;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonRestKlient;

public class OrganisasjonTjenesteTest {

    private OrganisasjonTjeneste organisasjonTjeneste;
    private OrganisasjonRestKlient organisasjonConsumer;

    private static final String ORG_NUMMER = "973861778";

    @BeforeEach
    public void setUp() {
        organisasjonConsumer = mock(OrganisasjonRestKlient.class);
        organisasjonTjeneste = new OrganisasjonTjeneste(organisasjonConsumer);
        var responsmock = mock(OrganisasjonEReg.class);
        when(responsmock.getNavn()).thenReturn("STATOIL ASA AVD STATOIL SOKKELVIRKSOMHET");
        when(organisasjonConsumer.hentOrganisasjon(any())).thenReturn(responsmock);
    }


    @Test
    public void skal_test_hentOrganisasjonInfo_medGyldigOrgnummer()  {
        Optional<OrganisasjonInfo> organisasjonInfo = organisasjonTjeneste.hentOrganisasjonInfo(ORG_NUMMER);
        Assertions.assertThat(organisasjonInfo).isPresent();
        Assertions.assertThat(organisasjonInfo.get().getNavn()).isEqualToIgnoringCase("STATOIL ASA AVD STATOIL SOKKELVIRKSOMHET");
    }



}
