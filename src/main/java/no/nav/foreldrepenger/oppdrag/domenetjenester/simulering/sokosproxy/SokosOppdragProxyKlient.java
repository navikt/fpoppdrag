package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "sokos.oppdrag.proxy.url",
    endpointDefault = "http://sokos-oppdrag-proxy.beregningsplattform/api/osSimulerBeregningApi/simulerBeregning",
    scopesProperty = "sokos.oppdrag.proxy.scope", scopesDefault = "api://prod-fss.beregningsplattform.sokos-oppdrag-proxy/.default")
public class SokosOppdragProxyKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SokosOppdragProxyKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;


    public SokosOppdragProxyKlient() {
        this(RestClient.client());
    }

    public SokosOppdragProxyKlient(RestClient restClient) {
        this(restClient, RestConfig.forClient(SokosOppdragProxyKlient.class));
    }

    public SokosOppdragProxyKlient(RestClient restClient, RestConfig config) {
        this.restClient = restClient;
        this.restConfig = config;
    }

    public List<SokosSimuleringRespons.Respons> utførSimulering(OppdragskontrollDto oppdragskontrollDto, YtelseType ytelseType,
                                                              boolean utenInntrekk) {
        return SokosSimuleringRequestMapper.map(oppdragskontrollDto, ytelseType, utenInntrekk).stream()
            .map(dto -> simuler(dto, oppdragskontrollDto.behandlingId(), utenInntrekk))
            .toList();
    }

    private SokosSimuleringRespons.Respons simuler(SokosSimuleringRequest dto, String behandlingId, boolean utenInntrekk) {
        var request = RestRequest.newPOSTJson(dto, restConfig.endpoint(), restConfig);
        var httpRespons = restClient.sendReturnUnhandledNoRetry(request);
        if (httpRespons.statusCode() == 503) {
            throw new SokosOppdragNedetidException();
        }
        if (httpRespons.statusCode() != 200) {
            LOG.info("Sokos-simulering feilet. behandlingId={} utenInntrekk={} httpStatus={}",
                behandlingId, utenInntrekk, httpRespons.statusCode());
            throw new IntegrasjonException("FPO-468818", "Uventet HTTP-status fra Sokos");
        }
        var envelope = Objects.requireNonNull(fromJson(httpRespons.body(), SokosSimuleringRespons.class), "Mangler Sokos-respons");
        var operation = Objects.requireNonNull(envelope.operation(), "Mangler Sokos-operation");
        var container = Objects.requireNonNull(operation.container(), "Mangler ZS1OCont");
        var respons = Objects.requireNonNull(container.responsSimulering(), "Mangler responsSimulering");
        if (!Integer.valueOf(0).equals(respons.statusKode())) {
            LOG.info("Sokos-simulering status. behandlingId={} utenInntrekk={} statusKode={}",
                behandlingId, utenInntrekk, respons.statusKode());
            if (!Integer.valueOf(4).equals(respons.statusKode())) {
                throw new IntegrasjonException("FPO-468819", "Feilstatus i Sokos-simulering");
            }
        }
        return respons;
    }
}
