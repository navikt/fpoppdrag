package no.nav.foreldrepenger.oppdrag.oppdragslager;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * En basis {@link Entity} klasse som håndtere felles standarder for utformign
 * av tabeller (eks. sporing av hvem som har opprettet en rad og når).
 */
@MappedSuperclass
public abstract class BaseCreateableEntitet implements Serializable {

    private static final String BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES = "VL";

    @Column(name = "opprettet_av", nullable = false, updatable = false)
    private String opprettetAv;

    @Column(name = "opprettet_tid", nullable = false, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    @PrePersist
    protected void onCreate() {
        this.opprettetAv = finnBrukernavn();
        this.opprettetTidspunkt = LocalDateTime.now();
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    protected static String finnBrukernavn() {
        var brukerident = SubjectHandler.getSubjectHandler().getUid();
        return brukerident != null ? brukerident : BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES;
    }
}
