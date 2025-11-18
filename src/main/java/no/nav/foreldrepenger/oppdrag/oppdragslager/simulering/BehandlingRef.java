package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BehandlingRef implements Serializable {

    @Column(name = "behandling_id", nullable = false)
    private Long behandlingId;

    private BehandlingRef() {
        // Hibernate
    }

    public BehandlingRef(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BehandlingRef that = (BehandlingRef) obj;
        return Objects.equals(behandlingId, that.behandlingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<behandlingId=" + behandlingId + ">";
    }
}
