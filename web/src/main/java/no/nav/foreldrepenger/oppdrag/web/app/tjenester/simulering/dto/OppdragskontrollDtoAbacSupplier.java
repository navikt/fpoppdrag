package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.function.Function;

import no.nav.foreldrepenger.kontrakter.simulering.request.OppdragskontrollDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class OppdragskontrollDtoAbacSupplier {

    public static class Supplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (OppdragskontrollDto) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, req.behandlingId());
        }
    }
}
