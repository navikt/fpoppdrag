package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.FpWsProxySimuleringKlient.FeilType.OPPDRAG_FORVENTET_NEDETID;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPWSPROXY)
public class FpWsProxySimuleringKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointStartSimulering;

    public FpWsProxySimuleringKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointStartSimulering = UriBuilder.fromUri(restConfig.endpoint()).path("/simulering/start").build();
    }

    public List<BeregningDto> utførSimuleringMedExceptionHandling(OppdragskontrollDto oppdragskontrollDto, YtelseType ytelseType, boolean utenInntrekk) {
        var target = UriBuilder.fromUri(endpointStartSimulering).queryParam("uten_inntrekk", utenInntrekk);
        if (ytelseType != null) target.queryParam("ytelse_type", ytelseType);
        var request = RestRequest.newPOSTJson(oppdragskontrollDto, target.build(), restConfig);
        return handleResponse(restClient.sendReturnUnhandled(request))
                .map(r -> Arrays.asList(fromJson(r, BeregningDto[].class)))
                .orElse(new ArrayList<>());
    }

    private static Optional<String> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HTTP_OK && status < HTTP_MULT_CHOICE) {
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else {
            if (status == HTTP_UNAVAILABLE && erDetForventetNedetid(body)) { // fpwsproxy kaster 503 feil ved nedetid av OS
                throw new OppdragNedetidException();
            }
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }

    private static boolean erDetForventetNedetid(String body) {
        try {
            return OPPDRAG_FORVENTET_NEDETID.equals(fromJson(body, FeilDto.class).type());
        } catch (Exception e) {
            return false;
        }
    }

    // TODO: Konsolider FeilDto fra web modul. Flytt til felles? kontrakter?
    public record FeilDto(FeilType type) {
    }

    public enum FeilType {
        MANGLER_TILGANG_FEIL,
        TOMT_RESULTAT_FEIL,
        OPPDRAG_FORVENTET_NEDETID,
        GENERELL_FEIL,
        ;
    }
}
