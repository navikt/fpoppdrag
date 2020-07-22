package no.nav.foreldrepenger.oppdrag;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.foreldrepenger.oppdrag.util.XmlStringFieldFikser;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class OppdragConsumerImpl implements OppdragConsumer {

    public static final String SERVICE_IDENTIFIER = "OppdragService";

    /** oppdragssytemet (OS) har offisiell åpningstid mandag-fredag 0700-1900, men det er ofte åpent utenom de offisielle åpningstidene.
     *  utenom åpningstid styrer vi logging slik at feilmeldinger fra OS som vanligvis forekommer ved nedetid logges som Info, for å ikke skape støy i loggene
     */
    private static final Set<DayOfWeek> OPPDRAG_ÅPNE_DAGER = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    private static final LocalTime OPPDRAG_ÅPNINGSTID_START = LocalTime.of(7, 0);
    private static final LocalTime OPPDRAG_ÅPNINGSTID_SLUTT = LocalTime.of(19, 0);
    static final List<String> TYPISKE_FEILMELDING_NÅR_OPPDRAG_ER_NEDE = Arrays.asList("Unexpected EOF in prolog", "Could not send Message", "Error writing request body to server");

    private SimulerFpService port;

    public OppdragConsumerImpl(SimulerFpService port) {
        this.port = port;
    }

    @Override
    public SimulerBeregningResponse hentSimulerBeregningResponse(SimulerBeregningRequest simulerBeregningRequest) throws SimulerBeregningFeilUnderBehandling {
        try {
            SimulerBeregningResponse response = port.simulerBeregning(simulerBeregningRequest);
            XmlStringFieldFikser.stripTrailingSpacesFromStrings(response);
            return response;
        } catch (SOAPFaultException e) {
            if (feiletPgaOppdragsystemetUtenforÅpningstid(e)) {
                throw OppdragConsumerFeil.FACTORY.oppdragsystemetHarNedeteid(e).toException();
            }
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private boolean feiletPgaOppdragsystemetUtenforÅpningstid(SOAPFaultException e) {
        return !innenforÅpningstid() && TYPISKE_FEILMELDING_NÅR_OPPDRAG_ER_NEDE.stream().anyMatch(typisk -> e.getMessage().contains(typisk));
    }

    boolean innenforÅpningstid() {
        LocalDateTime nå = LocalDateTime.now();
        return OPPDRAG_ÅPNE_DAGER.contains(nå.getDayOfWeek())
                && nå.toLocalTime().isAfter(OPPDRAG_ÅPNINGSTID_START)
                && nå.toLocalTime().isBefore(OPPDRAG_ÅPNINGSTID_SLUTT);
    }

}
