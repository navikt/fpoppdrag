package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.FPoppdragEntityManagerAwareExtension;

@ExtendWith(FPoppdragEntityManagerAwareExtension.class)
public class SimuleringXmlRepositoryTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final String REQ_XML = "<xml></xml>";
    private static final String RES_XML = "<xml></xml>";

    private SimuleringXmlRepository simuleringXmlRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringXmlRepository = new SimuleringXmlRepository(entityManager);
    }

    @Test
    public void test_lagrer_simulering_xml() {
        SimuleringXml simuleringXml = SimuleringXml.builder()
                .medEksternReferanse(BEHANDLING_ID)
                .medRequest(REQ_XML)
                .medResponse(RES_XML)
                .build();

        simuleringXmlRepository.lagre(simuleringXml);

        List<SimuleringXml> funnet = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID);
        assertThat(funnet).contains(simuleringXml);
    }

    @Test
    public void test_oppdaterer_simulering_xml() {
        SimuleringXml simuleringXml1 = SimuleringXml.builder()
                .medEksternReferanse(BEHANDLING_ID)
                .medRequest(REQ_XML)
                .build();

        simuleringXmlRepository.lagre(simuleringXml1);

        List<SimuleringXml> funnet1 = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID);
        assertThat(funnet1).hasSize(1);
        assertThat(funnet1).contains(simuleringXml1);
        assertThat(funnet1.stream().findFirst().get().getResponseXml()).isNullOrEmpty();

        SimuleringXml simuleringXml2 = SimuleringXml.builder()
                .medEksternReferanse(BEHANDLING_ID)
                .medRequest(REQ_XML)
                .medResponse(RES_XML)
                .build();

        simuleringXmlRepository.lagre(simuleringXml2);
        List<SimuleringXml> funnet2 = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID);
        assertThat(funnet2).hasSize(1);
        assertThat(funnet2).contains(simuleringXml2);
        assertThat(funnet2.stream().findFirst().get().getResponseXml()).isEqualTo(RES_XML);
    }

}
