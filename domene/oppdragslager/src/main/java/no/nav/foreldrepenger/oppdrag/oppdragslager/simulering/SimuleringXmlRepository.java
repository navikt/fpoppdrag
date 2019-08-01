package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.List;

public interface SimuleringXmlRepository {

    void lagre(SimuleringXml simuleringXml);

    void nyTransaksjon();

    List<SimuleringXml> hentSimuleringXml(Long behandlingId);

}
