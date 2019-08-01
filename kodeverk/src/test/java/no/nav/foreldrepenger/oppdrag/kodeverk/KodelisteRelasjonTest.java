package no.nav.foreldrepenger.oppdrag.kodeverk;

import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class KodelisteRelasjonTest {

    @Test
    public void constructor(){
        String kodeverk1 = "kodeverk1";
        String kode1 = "kode1";
        String kodeverk2 = "kodeverk2";
        String kode2 = "kode2";
        LocalDate gyldigFom = LocalDate.now();
        LocalDate gyldigTom = LocalDate.now();
        KodelisteRelasjon r = new KodelisteRelasjon(kodeverk1, kode1, kodeverk2, kode2, gyldigFom, gyldigTom);
        assertEquals(r.getKodeverk1(), kodeverk1);
        assertEquals(r.getKode1(), kode1);
        assertEquals(r.getKodeverk2(), kodeverk2);
        assertEquals(r.getKode2(), kode2);
        assertEquals(r.getGyldigFom(), gyldigFom);
        assertEquals(r.getGyldigTom(), gyldigTom);
    }

}
