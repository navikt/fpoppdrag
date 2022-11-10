package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag110;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragSkjemaConstants;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.TestResourceLoader;
import no.nav.foreldrepenger.xmlutils.JaxbHelper;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag;

@Deprecated // Flyttet til fp-ws-proxy
public class OppdragMapperTest {

    @Test
    public void test_skalMappeOppdrag110TilOppdrag() throws Exception {
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker.xml");

        no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag oppdragWrapper = JaxbHelper.unmarshalAndValidateXMLWithStAX(OppdragSkjemaConstants.JAXB_CLASS, xml, OppdragSkjemaConstants.XSD_LOCATION);

        Oppdrag110 oppdrag110 = oppdragWrapper.getOppdrag110();
        Oppdrag oppdrag = OppdragMapper.mapTilSimuleringOppdrag(oppdragWrapper.getOppdrag110());

        assertThat(oppdrag.getFagsystemId()).isEqualToIgnoringCase(oppdrag110.getFagsystemId());
        assertThat(oppdrag.getOppdragslinje()).hasSize(oppdrag110.getOppdragsLinje150().size());

    }

    @Test
    public void test_skalMappeOppdrag110MedOmpostering116TilOppdrag() throws Exception {
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_3.xml");

        no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag oppdragWrapper = JaxbHelper.unmarshalAndValidateXMLWithStAX(OppdragSkjemaConstants.JAXB_CLASS, xml, OppdragSkjemaConstants.XSD_LOCATION);

        Oppdrag110 oppdrag110 = oppdragWrapper.getOppdrag110();
        Oppdrag oppdrag = OppdragMapper.mapTilSimuleringOppdrag(oppdragWrapper.getOppdrag110());

        assertThat(oppdrag.getFagsystemId()).isEqualToIgnoringCase(oppdrag110.getFagsystemId());
        assertThat(oppdrag.getOppdragslinje()).hasSize(oppdrag110.getOppdragsLinje150().size());
        assertThat(oppdrag.getOmpostering()).isNotNull();
        assertThat(oppdrag.getOmpostering().getOmPostering()).isEqualTo(oppdrag110.getOmpostering116().getOmPostering());
        assertThat(oppdrag.getOmpostering().getSaksbehId()).isEqualTo(oppdrag110.getOmpostering116().getSaksbehId());
    }
}
