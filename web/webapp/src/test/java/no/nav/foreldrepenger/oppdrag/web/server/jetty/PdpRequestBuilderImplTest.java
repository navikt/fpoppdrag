package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;

public class PdpRequestBuilderImplTest {

    private static final String DUMMY_ID_TOKEN = "dfksjkfjdgskjhkjuh";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PdpRequestBuilderImpl pdpRequestBuilder = new PdpRequestBuilderImpl();

    @Test
    public void skal_feil_lagPdpRequest_med_ugyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter aktørIdAttribute = AbacDataAttributter.opprett().leggTilDokumentDataId(23432545634L);
        abacAttributtSamling.leggTil(aktørIdAttribute);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Utvikler-feil: Attributten dokumentDataID er ikke støttet p.t. Bruker du riktig attributt? Hvis ja, legg til støtte");

        pdpRequestBuilder.lagPdpRequest(abacAttributtSamling);
    }

    @Test
    public void skal_lagPdpRequest_med_gyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter behandlingIdAttribute = AbacDataAttributter.opprett().leggTilAktørId("000123");
        abacAttributtSamling.leggTil(behandlingIdAttribute);

        PdpRequest pdpRequest = pdpRequestBuilder.lagPdpRequest(abacAttributtSamling);
        assertThat(pdpRequest).isNotNull();
    }

    private AbacAttributtSamling byggAbacAttributtSamling(BeskyttetRessursActionAttributt actionType) {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(actionType);
        attributtSamling.setResource(BeskyttetRessursResourceAttributt.FAGSAK);
        return attributtSamling;
    }
}
