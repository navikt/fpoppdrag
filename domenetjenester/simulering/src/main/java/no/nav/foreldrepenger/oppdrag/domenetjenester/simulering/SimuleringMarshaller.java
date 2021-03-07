package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.vedtak.exception.TekniskException;

class SimuleringMarshaller {
    private static ConcurrentHashMap<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<>();

    public static String marshall(long behandlingId, SimulerBeregningResponse response) {
        try {
            return marshall(SimulerBeregningResponse.class, response);
        } catch (JAXBException e) {
            throw new TekniskException("FPO-852523", String.format("Kunne ikke marshalle simuleringresultatet til XML for behandlingId=%s", behandlingId), e);
        }
    }

    public static String marshall(long behandlingId, SimulerBeregningRequest request) {
        try {
            return marshall(SimulerBeregningRequest.class, request);
        } catch (JAXBException e) {
            throw new TekniskException("FPO-852524", String.format("Kunne ikke marshalle simulering request til XML for behandlingId=%s", behandlingId), e);
        }
    }

    private static String marshall(Class<?> klasse, Object request) throws JAXBException {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        Marshaller marshaller = getContext(klasse).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(request, stringWriter);
        return stringWriter.toString();
    }

    private static JAXBContext getContext(Class<?> klasse) {
        return contextCache.computeIfAbsent(klasse, k -> {
            try {
                return JAXBContext.newInstance(k);
            } catch (JAXBException e) {
                throw new TekniskException("FPO-285590", "Klarte ikke lage JAXBcontext");
            }
        });
    }

}