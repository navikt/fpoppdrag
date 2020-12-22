package no.nav.foreldrepenger.oppdrag;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.util.XmlStringFieldFikser;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.system.os.tjenester.simulerfpservice.feil.FeilUnderBehandling;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;

public class OppdragConsumerImpl implements OppdragConsumer {

    /** oppdragssytemet (OS) har offisiell åpningstid mandag-fredag 0700-1900, men det er ofte åpent utenom de offisielle åpningstidene.
     *  utenom åpningstid styrer vi logging slik at feilmeldinger fra OS som vanligvis forekommer ved nedetid logges som Info, for å ikke skape støy i loggene
     */
    private static final Set<DayOfWeek> OPPDRAG_ÅPNE_DAGER = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    private static final LocalTime OPPDRAG_ÅPNINGSTID_START = LocalTime.of(7, 0);
    private static final LocalTime OPPDRAG_ÅPNINGSTID_SLUTT = LocalTime.of(19, 0);
    static final List<String> TYPISKE_FEILMELDING_NÅR_OPPDRAG_ER_NEDE = Arrays.asList("Unexpected EOF in prolog", "Could not send Message", "Error writing request body to server");

    private static final Logger log = LoggerFactory.getLogger(OppdragConsumerImpl.class);

    private SimulerFpService port;

    public OppdragConsumerImpl(SimulerFpService port) {
        this.port = port;
    }

    @Override
    public SimulerBeregningResponse hentSimulerBeregningResponse(SimulerBeregningRequest simulerBeregningRequest, List<String> source) {
        try {
            SimulerBeregningResponse response = port.simulerBeregning(simulerBeregningRequest);
            XmlStringFieldFikser.stripTrailingSpacesFromStrings(response);
            return response;
        } catch (SimulerBeregningFeilUnderBehandling e) {
            FeilUnderBehandling fault = e.getFaultInfo();
            log.warn("Simulering feiler for request {}", source);
            throw OppdragConsumerFeil.FACTORY.feilUnderBehandlingAvSimulering(fault.getErrorSource(), fault.getErrorType(), fault.getErrorMessage(), fault.getRootCause(), fault.getDateTimeStamp(), e).toException();
        } catch (WebServiceException e) {
            if (feiletPgaOppdragsystemetUtenforÅpningstid(e)) {
                throw OppdragConsumerFeil.FACTORY.oppdragsystemetHarNedetid(e).toException();
            }
            throw OppdragConsumerFeil.FACTORY.feilUnderKallTilSimuleringtjenesten(e).toException();
        }
    }

    private boolean feiletPgaOppdragsystemetUtenforÅpningstid(WebServiceException e) {
        return !innenforÅpningstid() && TYPISKE_FEILMELDING_NÅR_OPPDRAG_ER_NEDE.stream().anyMatch(typisk -> e.getMessage().contains(typisk));
    }

    boolean innenforÅpningstid() {
        LocalDateTime nå = LocalDateTime.now();
        return OPPDRAG_ÅPNE_DAGER.contains(nå.getDayOfWeek())
                && nå.toLocalTime().isAfter(OPPDRAG_ÅPNINGSTID_START)
                && nå.toLocalTime().isBefore(OPPDRAG_ÅPNINGSTID_SLUTT);
    }

}
