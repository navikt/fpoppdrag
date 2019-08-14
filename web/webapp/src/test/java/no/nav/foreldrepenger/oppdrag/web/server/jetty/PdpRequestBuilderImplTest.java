package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip.PipRepository;
import no.nav.foreldrepenger.oppdrag.web.server.jetty.abac.PdpRequestBuilderImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class PdpRequestBuilderImplTest {

    private static final String DUMMY_ID_TOKEN = "dfksjkfjdgskjhkjuh";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private PipRepository pipRepository = new PipRepository(repositoryRule.getEntityManager());
    private PdpRequestBuilderImpl pdpRequestBuilder = new PdpRequestBuilderImpl(pipRepository);

    @Test
    public void skal_feil_lagPdpRequest_med_ugyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter aktørIdAttribute = AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.DOKUMENT_DATA_ID, "foo");
        abacAttributtSamling.leggTil(aktørIdAttribute);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Utvikler-feil: ikke-implementert støtte for minst en av typene: [DOKUMENT_DATA_ID]");

        pdpRequestBuilder.lagPdpRequest(abacAttributtSamling);
    }

    @Test
    public void skal_lagPdpRequest_med_gyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter behandlingIdAttribute = AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 000123L);
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
