package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.BaseEntitet;

/**
 * Denne klassen er en ren avbildning fra Oppdragsløsningens meldingsformater.
 * Navngivning følger ikke nødvendigvis Vedtaksløsningens navnestandarder.
 */
@Entity(name = "Avstemming115")
@Table(name = "OKO_AVSTEMMING_115")
public class Avstemming115 extends BaseEntitet{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_AVSTEMMING_115")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kode_komponent", nullable = false)
    private String kodekomponent;

    // Avstemmingsnøkkel - brukes til å matche oppdragsmeldinger i avstemmingen
    @Column(name = "noekkel_avstemming", nullable = false)
    private String noekkelAvstemming;

    @Column(name = "tidspunkt_melding", nullable = false)
    private String tidspunktMelding;

    @Column(name = "oppdrag110_id")
    private Long oppdrag110Id;

    public Avstemming115() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodekomponent() {
        return kodekomponent;
    }

    public void setKodekomponent(String kodekomponent) {
        this.kodekomponent = kodekomponent;
    }

    public String getNoekkelAvstemming() {
        return noekkelAvstemming;
    }

    public void setNoekkelAvstemming(String noekkelAvstemming) {
        this.noekkelAvstemming = noekkelAvstemming;
    }

    public String getTidspunktMelding() {
        return tidspunktMelding;
    }

    public void setTidspunktMelding(String tidspunktMelding) {
        this.tidspunktMelding = tidspunktMelding;
    }

    public Long getOppdrag110Id() {
        return oppdrag110Id;
    }

    @Override
    public boolean equals(Object object){
        if (object == this) {
            return true;
        }
        if (!(object instanceof Avstemming115)) {
            return false;
        }
        Avstemming115 avstemnokler115 = (Avstemming115) object;
        return Objects.equals(kodekomponent, avstemnokler115.getKodekomponent())
                && Objects.equals(noekkelAvstemming, avstemnokler115.getNoekkelAvstemming())
                && Objects.equals(tidspunktMelding, avstemnokler115.getTidspunktMelding());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodekomponent, noekkelAvstemming, tidspunktMelding);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String kodekomponent;
        private String noekkelAvstemming;
        private String tidspunktMelding;

        public Builder medKodekomponent(String kodekomponent) { this.kodekomponent = kodekomponent; return this; }

        public Builder medNoekkelAvstemming(String noekkelAvstemming) { this.noekkelAvstemming = noekkelAvstemming; return this; }

        public Builder medTidspunktMelding(String tidspunktMelding) { this.tidspunktMelding = tidspunktMelding; return this; }

        public Avstemming115 build() {
            verifyStateForBuild();
            Avstemming115 avstemming115 = new Avstemming115();
            avstemming115.kodekomponent = kodekomponent;
            avstemming115.noekkelAvstemming = noekkelAvstemming;
            avstemming115.tidspunktMelding = tidspunktMelding;

            return avstemming115;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kodekomponent, "kodekomponent");
            Objects.requireNonNull(noekkelAvstemming, "noekkelAvstemming");
            Objects.requireNonNull(tidspunktMelding, "tidspunktMelding");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "kodekomponent=" + kodekomponent + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "noekkelAvstemming=" + noekkelAvstemming + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "tidspunktMelding=" + tidspunktMelding + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
