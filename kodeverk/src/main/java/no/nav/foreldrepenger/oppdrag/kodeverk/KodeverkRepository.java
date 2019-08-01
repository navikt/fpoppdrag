package no.nav.foreldrepenger.oppdrag.kodeverk;

import java.util.List;

/**
 * Få tilgang til kodeverk.
 */
public interface KodeverkRepository {

    /**
     * Finn instans av Kodeliste innslag for angitt kode verdi.
     */
    <V extends Kodeliste> V finn(Class<V> cls, String kode);

    /**
     * Hent alle innslag for en gitt Kodeliste.
     */
    <V extends Kodeliste> List<V> hentAlle(Class<V> cls);
}
