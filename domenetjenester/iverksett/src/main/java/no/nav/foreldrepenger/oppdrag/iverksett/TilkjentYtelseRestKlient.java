package no.nav.foreldrepenger.oppdrag.iverksett;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.TilkjentYtelse;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class TilkjentYtelseRestKlient {

    private static final Logger logger = LoggerFactory.getLogger(TilkjentYtelseRestKlient.class);

    private static final String FPSAK_TILKJENT_YTELSE_URL = "/behandling/beregningsresultat/tilkjentytelse";
    private static final String FPSAK_BASE_URL = "http://fpsak/fpsak/api";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";

    private URI uriHentTilkjentYtelse;
    private OidcRestClient restClient;

    TilkjentYtelseRestKlient() {
        //for CDI proxy
    }

    @Inject
    public TilkjentYtelseRestKlient(OidcRestClient restClient) {
        this.restClient = restClient;
        uriHentTilkjentYtelse = URI.create(getFpsakBaseUrl() + FPSAK_TILKJENT_YTELSE_URL);
    }

    public TilkjentYtelse hentTilkjentYtelse(long behandlingId) {
        BehandlingIdDto behandlingIdDto = new BehandlingIdDto(behandlingId);
        return restClient.post(uriHentTilkjentYtelse, behandlingIdDto, TilkjentYtelse.class);
    }

    static String getFpsakBaseUrl() {
        String overrideUrl = Environment.current().getProperty(FPSAK_OVERRIDE_URL);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            logger.info("Overstyrte URL til fpsak til {}", overrideUrl);
            return overrideUrl;
        } else {
            return FPSAK_BASE_URL;
        }

    }

    static class BehandlingIdDto {
        private long behandlingId;

        public BehandlingIdDto(long behandlingId) {
            this.behandlingId = behandlingId;
        }

        public long getBehandlingId() {
            return behandlingId;
        }
    }
}
