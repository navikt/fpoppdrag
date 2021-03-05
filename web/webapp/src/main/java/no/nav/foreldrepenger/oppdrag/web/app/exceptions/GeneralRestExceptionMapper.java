package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.foreldrepenger.oppdrag.OppdragNedetidException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.jpa.TomtResultatException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

// TODO (tor) Har berre fått denne til å fungera med ApplicationException. Dermed blir denne mapperen heilt
// generell. (Eigen mapper for ConstraintViolationException.)

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<ApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    @Override
    public Response toResponse(ApplicationException exception) {
        Throwable cause = exception.getCause();

        if (cause instanceof Valideringsfeil) { //NOSONAR
            return handleValideringsfeil((Valideringsfeil) cause);
        } else if (cause instanceof TomtResultatException) { //NOSONAR
            return handleTomtResultatFeil((TomtResultatException) cause);
        }

        loggTilApplikasjonslogg(cause);
        String callId = MDCOperations.getCallId();

        if (cause instanceof VLException) { //NOSONAR
            return handleVLException((VLException) cause, callId);
        }

        return handleGenerellFeil(cause, callId);
    }

    private Response handleTomtResultatFeil(TomtResultatException tomtResultatException) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new FeilDto(FeilType.TOMT_RESULTAT_FEIL, tomtResultatException.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response handleValideringsfeil(Valideringsfeil valideringsfeil) {
        List<String> feltNavn = valideringsfeil.getFeltFeil().stream().map(FeltFeilDto::getNavn).collect(Collectors.toList());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(
                        FeltValideringFeil.feltverdiKanIkkeValideres(feltNavn).toString(),
                        valideringsfeil.getFeltFeil()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response handleVLException(VLException vlException, String callId) {

        if (vlException instanceof ManglerTilgangException) {
            return ikkeTilgang(vlException);
        } else if (vlException instanceof OppdragNedetidException) {
            return ikkeTilgjengelig(callId, vlException);
        } else {
            return serverError(callId, vlException);
        }
    }

    private Response serverError(String callId, VLException feil) {
        String feilmelding = getVLExceptionFeilmelding(callId, feil);
        FeilType feilType = FeilType.GENERELL_FEIL;
        return Response.serverError()
                .entity(new FeilDto(feilType, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response ikkeTilgjengelig(String callId, VLException feil) {
        String feilmelding = getVLExceptionFeilmelding(callId, feil);
        FeilType feilType = FeilType.GENERELL_FEIL;
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new FeilDto(feilType, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response ikkeTilgang(VLException feil) {
        String feilmelding = feil.toString();
        FeilType feilType = FeilType.MANGLER_TILGANG_FEIL;
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new FeilDto(feilType, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String getVLExceptionFeilmelding(String callId, VLException feil) {
        String feilbeskrivelse = feil.toString();
        return "Det oppstod en feil: "
                + avsluttMedPunktum(feilbeskrivelse)
                + ". Referanse-id: " + callId;
    }

    private Response handleGenerellFeil(Throwable cause, String callId) {
        String generellFeilmelding = "Det oppstod en serverfeil: " + cause.getMessage() + ". Meld til support med referanse-id: " + callId; //$NON-NLS-1$ //$NON-NLS-2$
        return Response.serverError()
                .entity(new FeilDto(FeilType.GENERELL_FEIL, generellFeilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String avsluttMedPunktum(String tekst) {
        return tekst + (tekst.endsWith(".") ? " " : ". "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void loggTilApplikasjonslogg(Throwable cause) {
        if (cause instanceof OppdragNedetidException) {
            LOGGER.info(cause.toString());
        } else if (cause instanceof VLException) {
            LOGGER.warn(cause.toString(), cause.getCause());
        } else {
            String message = cause.getMessage() != null ? LoggerUtils.removeLineBreaks(cause.getMessage()) : "";
            LOGGER.error("Fikk uventet feil:" + message, cause); //NOSONAR //$NON-NLS-1$
        }

        // key for å tracke prosess -- nullstill denne
        MDC.remove("prosess");  //$NON-NLS-1$
    }

}
