package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "fpwsproxy.rs.url", endpointDefault = "http://fp-ws-proxy")
public class FpWsProxySimuleringKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointStartSimulering;

    public FpWsProxySimuleringKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointStartSimulering = UriBuilder.fromUri(restConfig.endpoint()).path("/api/simulering/start").build();
    }
    public List<BeregningDto> utførSimulering(OppdragskontrollDto oppdragskontrollDto, YtelseType ytelseType, boolean utenInntrekk) {
        var target = UriBuilder.fromUri(endpointStartSimulering)
                .queryParam("uten_inntrekk", utenInntrekk);
        if (ytelseType != null) target.queryParam("ytelse_type", ytelseType);
        var request = RestRequest.newPOSTJson(oppdragskontrollDto, target.build(), restConfig);
        var beregningDtos = restClient.send(request, BeregningDto[].class);
        return Arrays.asList(beregningDtos);
    }
}
