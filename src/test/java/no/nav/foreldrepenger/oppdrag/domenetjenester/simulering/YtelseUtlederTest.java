package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

class YtelseUtlederTest {

    @Test
    void utledForValue() {
        assertThat(YtelseUtleder.utledFor(Fagområde.FP)).isEqualTo(YtelseType.FP);
    }

    @Test
    void utledForNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->  YtelseUtleder.utledFor(null)
        );
        assertEquals("Fagområde kan ikke våre null.", exception.getMessage());
    }
}