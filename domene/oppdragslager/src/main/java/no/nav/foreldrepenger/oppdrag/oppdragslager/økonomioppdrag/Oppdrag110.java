package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.vedtak.felles.jpa.BaseEntitet;

/**
 * Denne klassen er en ren avbildning fra Oppdragsløsningens meldingsformater.
 * Navngivning følger ikke nødvendigvis Vedtaksløsningens navnestandarder.
 */
@Entity(name = "Oppdrag110")
@Table(name = "OKO_OPPDRAG_110")
public class Oppdrag110 extends BaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_OPPDRAG_110")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kode_aksjon", nullable = false)
    private String kodeAksjon;

    @Column(name = "kode_endring", nullable = false)
    private String kodeEndring;

    @Column(name = "kode_fagomraade", nullable = false)
    private String kodeFagomraade;

    @Column(name = "fagsystem_id", nullable = false)
    private long fagsystemId;

    @Column(name = "utbet_frekvens", nullable = false)
    private String utbetFrekvens;

    @Column(name = "oppdrag_gjelder_id", nullable = false)
    private String oppdragGjelderId;

    @Column(name = "dato_oppdrag_gjelder_fom", nullable = false)
    private LocalDate datoOppdragGjelderFom;

    @Column(name = "saksbeh_id", nullable = false)
    private String saksbehId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrag_kontroll_id", nullable = false, updatable = false)
    @JsonBackReference
    private Oppdragskontroll oppdragskontroll;

    /* bruker @ManyToOne siden JPA ikke støtter OneToOne join på non-PK column. */
    @ManyToOne(cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "avstemming115_id", nullable = false)
    private Avstemming115 avstemming115;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<Oppdragslinje150> oppdragslinje150Liste = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<Oppdragsenhet120> oppdragsenhet120Liste = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<OppdragKvittering> oppdragKvitteringListe = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private Ompostering116 ompostering116;

    public Oppdrag110() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeAksjon() {
        return kodeAksjon;
    }

    public String getKodeEndring() {
        return kodeEndring;
    }

    public String getKodeFagomraade() {
        return kodeFagomraade;
    }

    public long getFagsystemId() {
        return fagsystemId;
    }

    public void setFagsystemId(long fagsystemId) {
        this.fagsystemId = fagsystemId;
    }

    public String getUtbetFrekvens() {
        return utbetFrekvens;
    }

    public String getOppdragGjelderId() {
        return oppdragGjelderId;
    }

    public LocalDate getDatoOppdragGjelderFom() {
        return datoOppdragGjelderFom;
    }

    public String getSaksbehId() {
        return saksbehId;
    }

    public Oppdragskontroll getOppdragskontroll() {
        return oppdragskontroll;
    }

    public void setOppdragskontroll(Oppdragskontroll oppdragskontroll) {
        this.oppdragskontroll = oppdragskontroll;
    }

    public List<Oppdragslinje150> getOppdragslinje150Liste() {
        return oppdragslinje150Liste;
    }

    protected void addOppdragslinje150(Oppdragslinje150 oppdragslinje150) {
        Objects.requireNonNull(oppdragslinje150, "oppdragslinje150");
        oppdragslinje150Liste.add(oppdragslinje150);
    }

    public List<Oppdragsenhet120> getOppdragsenhet120Liste() {
        return oppdragsenhet120Liste;
    }

    protected void addOppdragsenhet120(Oppdragsenhet120 oppdragsenhet120) {
        Objects.requireNonNull(oppdragsenhet120, "oppdragsenhet120");
        oppdragsenhet120Liste.add(oppdragsenhet120);
    }

    public List<OppdragKvittering> getOppdragKvitteringListe() {
        return oppdragKvitteringListe;
    }

    protected void addOppdragKvittering(OppdragKvittering oppdragKvittering) {
        Objects.requireNonNull(oppdragKvittering, "oppdragKvittering");
        oppdragKvitteringListe.add(oppdragKvittering);
    }

    public Avstemming115 getAvstemming115() {
        return avstemming115;
    }

    public void setAvstemming115(Avstemming115 avstemming115) {
        this.avstemming115 = avstemming115;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Oppdrag110)) {
            return false;
        }
        Oppdrag110 oppdr110 = (Oppdrag110) object;
        return Objects.equals(kodeAksjon, oppdr110.getKodeAksjon())
                && Objects.equals(kodeEndring, oppdr110.getKodeEndring())
                && Objects.equals(kodeFagomraade, oppdr110.getKodeFagomraade())
                && Objects.equals(fagsystemId, oppdr110.getFagsystemId())
                && Objects.equals(utbetFrekvens, oppdr110.getUtbetFrekvens())
                && Objects.equals(oppdragGjelderId, oppdr110.getOppdragGjelderId())
                && Objects.equals(datoOppdragGjelderFom, oppdr110.getDatoOppdragGjelderFom())
                && Objects.equals(saksbehId, oppdr110.getSaksbehId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeAksjon, kodeEndring, kodeFagomraade, fagsystemId, utbetFrekvens, oppdragGjelderId,
                datoOppdragGjelderFom, saksbehId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kodeAksjon;
        private String kodeEndring;
        private String kodeFagomraade;
        private Long fagsystemId;
        private String utbetFrekvens;
        private String oppdragGjelderId;
        private LocalDate datoOppdragGjelderFom;
        private String saksbehId;
        private Oppdragskontroll oppdragskontroll;
        private Avstemming115 avstemming115;
        private Ompostering116 ompostering116;

        public Builder medKodeAksjon(String kodeAksjon) {
            this.kodeAksjon = kodeAksjon;
            return this;
        }

        public Builder medKodeEndring(String kodeEndring) {
            this.kodeEndring = kodeEndring;
            return this;
        }

        public Builder medKodeFagomraade(String kodeFagomraade) {
            this.kodeFagomraade = kodeFagomraade;
            return this;
        }

        public Builder medFagSystemId(long fagsystemId) {
            this.fagsystemId = fagsystemId;
            return this;
        }

        public Builder medUtbetFrekvens(String utbetFrekvens) {
            this.utbetFrekvens = utbetFrekvens;
            return this;
        }

        public Builder medOppdragGjelderId(String oppdragGjelderId) {
            this.oppdragGjelderId = oppdragGjelderId;
            return this;
        }

        public Builder medDatoOppdragGjelderFom(LocalDate datoOppdrGjelderFom) {
            this.datoOppdragGjelderFom = datoOppdrGjelderFom;
            return this;
        }

        public Builder medSaksbehId(String saksbehId) {
            this.saksbehId = saksbehId;
            return this;
        }

        public Builder medOppdragskontroll(Oppdragskontroll oppdragskontroll) {
            this.oppdragskontroll = oppdragskontroll;
            return this;
        }

        public Builder medAvstemming115(Avstemming115 avstemming115) {
            this.avstemming115 = avstemming115;
            return this;
        }

        public Builder medOmpostering116(Ompostering116 ompostering116) {
            this.ompostering116 = ompostering116;
            return this;
        }

        public Oppdrag110 build() {
            verifyStateForBuild();
            Oppdrag110 oppdrag110 = new Oppdrag110();
            oppdrag110.kodeAksjon = kodeAksjon;
            oppdrag110.kodeEndring = kodeEndring;
            oppdrag110.kodeFagomraade = kodeFagomraade;
            oppdrag110.fagsystemId = fagsystemId;
            oppdrag110.utbetFrekvens = utbetFrekvens;
            oppdrag110.oppdragGjelderId = oppdragGjelderId;
            oppdrag110.datoOppdragGjelderFom = datoOppdragGjelderFom;
            oppdrag110.saksbehId = saksbehId;
            oppdrag110.oppdragskontroll = oppdragskontroll;
            oppdrag110.avstemming115 = avstemming115;
            oppdragskontroll.addOppdrag110(oppdrag110);

            if (ompostering116 != null) {
                oppdrag110.ompostering116 = ompostering116;
                ompostering116.setOppdrag110(oppdrag110);
            }

            return oppdrag110;
        }


        public void verifyStateForBuild() {
            Objects.requireNonNull(kodeAksjon, "kodeAksjon");
            Objects.requireNonNull(kodeEndring, "kodeEndring");
            Objects.requireNonNull(kodeFagomraade, "kodeFagomraade");
            Objects.requireNonNull(fagsystemId, "fagsystemId");
            Objects.requireNonNull(utbetFrekvens, "utbetFrekvens");
            Objects.requireNonNull(oppdragGjelderId, "oppdragGjelderId");
            Objects.requireNonNull(datoOppdragGjelderFom, "datoOppdragGjelderFom");
            Objects.requireNonNull(saksbehId, "saksbehId");
            Objects.requireNonNull(oppdragskontroll, "oppdragskontroll");
            Objects.requireNonNull(avstemming115, "avstemming115");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeAksjon=" + kodeAksjon + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeEndring=" + kodeEndring + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeFagomraade=" + kodeFagomraade + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "fagsystemId=" + fagsystemId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "utbetFrekvens=" + utbetFrekvens + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "oppdragGjelderId=" + oppdragGjelderId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "datoOppdragGjelderFom=" + datoOppdragGjelderFom + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "saksbehId=" + saksbehId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
