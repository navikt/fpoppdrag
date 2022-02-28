package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import static no.nav.foreldrepenger.oppdrag.web.app.abac.FPOppdragBeskyttetRessursAttributt.FAGSAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip.PipRepository;
import no.nav.foreldrepenger.oppdrag.web.server.jetty.abac.PdpRequestBuilderImpl;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@ExtendWith(MockitoExtension.class)
public class PdpRequestBuilderImplTest {

    private static final String DUMMY_ID_TOKEN = "dfksjkfjdgskjhkjuh";

    private PdpRequestBuilderImpl pdpRequestBuilder;

    @Mock
    PipRepository pipRepository;

    @BeforeEach
    void setUp() {
        pdpRequestBuilder = new PdpRequestBuilderImpl(pipRepository);
    }

    @Test
    public void skal_feil_lagPdpRequest_med_ugyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter aktørIdAttribute = AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.DOKUMENT_DATA_ID, "foo");
        abacAttributtSamling.leggTil(aktørIdAttribute);

        assertThatThrownBy(() -> pdpRequestBuilder.lagPdpRequest(abacAttributtSamling))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utvikler-feil: ikke-implementert støtte for minst en av typene: [DOKUMENT_DATA_ID]");
    }

    @Test
    public void skal_lagPdpRequest_med_gyldig_attributter() {
        AbacAttributtSamling abacAttributtSamling = byggAbacAttributtSamling(BeskyttetRessursActionAttributt.READ);
        AbacDataAttributter behandlingIdAttribute = AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 000123L);
        abacAttributtSamling.leggTil(behandlingIdAttribute);
        when(pipRepository.getAktørIdForBehandling(any())).thenReturn(Optional.of("1111111111111"));

        PdpRequest pdpRequest = pdpRequestBuilder.lagPdpRequest(abacAttributtSamling);
        assertThat(pdpRequest).isNotNull();
    }

    private AbacAttributtSamling byggAbacAttributtSamling(BeskyttetRessursActionAttributt actionType) {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN, AbacIdToken.TokenType.OIDC);
        attributtSamling.setActionType(actionType);
        attributtSamling.setResource(FAGSAK);
        return attributtSamling;
    }
}
