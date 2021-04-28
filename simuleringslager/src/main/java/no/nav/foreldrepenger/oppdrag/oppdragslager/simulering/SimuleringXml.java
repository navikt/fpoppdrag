package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity(name = "SimuleringXml")
@Table(name = "SIMULERING_XML")
public class SimuleringXml {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIMULERING_XML")
    private Long id;

    @Embedded
    @AttributeOverride(name = "behandlingId", column = @Column(name = "behandling_id", nullable = false))
    private BehandlingRef eksternReferanse;

    @Column(name = "request_xml", nullable = false)
    @Lob
    private String requestXml;

    @Column(name = "response_xml")
    @Lob
    private String responseXml;

    @Column(name = "fpsak_inputdata")
    @Lob
    private String fpsakInputdata;

    private SimuleringXml() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public BehandlingRef getEksternReferanse() {
        return eksternReferanse;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public String getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(String responseXml) {
        this.responseXml = responseXml;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        // forventer at xml for mottakere på samme behandling er forskjellige
        // forventer også at respons alltid er lik for samme request
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimuleringXml that = (SimuleringXml) o;
        return Objects.equals(eksternReferanse, that.eksternReferanse) &&
                Objects.equals(requestXml, that.requestXml);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eksternReferanse, requestXml);
    }

    public static class Builder {
        private SimuleringXml kladd;

        public Builder() {
            kladd = new SimuleringXml();
        }

        public Builder medEksternReferanse(Long behandlingId) {
            kladd.eksternReferanse = new BehandlingRef(behandlingId);
            return this;
        }

        public Builder medRequest(String requestXml) {
            kladd.requestXml = requestXml;
            return this;
        }

        public Builder medResponse(String responseXml) {
            kladd.responseXml = responseXml;
            return this;
        }

        public Builder medFpsakInput(String fpsakInputdata) {
            kladd.fpsakInputdata = fpsakInputdata;
            return this;
        }

        public SimuleringXml build() {
            return kladd;
        }
    }
}
