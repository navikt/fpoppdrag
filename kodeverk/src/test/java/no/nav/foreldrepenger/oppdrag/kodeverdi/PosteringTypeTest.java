package no.nav.foreldrepenger.oppdrag.kodeverdi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class PosteringTypeTest {

    @Test
    void values() {
        assertThat(PosteringType.values()).containsAll(List.of(PosteringType.YTEL, PosteringType.FEIL, PosteringType.SKAT, PosteringType.JUST, PosteringType.MOTP, PosteringType.TREK));
    }

    @Test
    void getOrNull() {
        assertThat(PosteringType.getOrNull(null)).isNull();
        assertThat(PosteringType.getOrNull("finnes_ikke")).isNull();
        assertThat(PosteringType.getOrNull(PosteringType.YTEL.name().toLowerCase())).isNull();
        assertThat(PosteringType.getOrNull(PosteringType.FEIL.name())).isEqualTo(PosteringType.FEIL);
    }

    @Test
    void valueOf() {
        assertThat(PosteringType.valueOf(PosteringType.SKAT.name())).isEqualTo(PosteringType.SKAT);
    }
}