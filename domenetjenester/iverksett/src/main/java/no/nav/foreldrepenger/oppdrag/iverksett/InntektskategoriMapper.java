package no.nav.foreldrepenger.oppdrag.iverksett;

import static java.util.Map.entry;

import java.util.Map;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1;
import no.nav.foreldrepenger.oppdrag.kodeverk.Inntektskategori;

class InntektskategoriMapper {

    private static final Map<TilkjentYtelseV1.Inntektskategori, Inntektskategori> INNTEKTSKATEGORI_MAP = Map.ofEntries(
            entry(TilkjentYtelseV1.Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER),
            entry(TilkjentYtelseV1.Inntektskategori.FRILANSER, Inntektskategori.FRILANSER),
            entry(TilkjentYtelseV1.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
            entry(TilkjentYtelseV1.Inntektskategori.DAGPENGER, Inntektskategori.DAGPENGER),
            entry(TilkjentYtelseV1.Inntektskategori.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER),
            entry(TilkjentYtelseV1.Inntektskategori.SJØMANN, Inntektskategori.SJØMANN),
            entry(TilkjentYtelseV1.Inntektskategori.DAGMAMMA, Inntektskategori.DAGMAMMA),
            entry(TilkjentYtelseV1.Inntektskategori.JORDBRUKER, Inntektskategori.JORDBRUKER),
            entry(TilkjentYtelseV1.Inntektskategori.FISKER, Inntektskategori.FISKER),
            entry(TilkjentYtelseV1.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER),
            entry(TilkjentYtelseV1.Inntektskategori.IKKE_RELEVANT, Inntektskategori.UDEFINERT)
    );

    private InntektskategoriMapper() {
    }

    static Inntektskategori map(TilkjentYtelseV1.Inntektskategori inntektskategoriV1) {
        var verdi = INNTEKTSKATEGORI_MAP.get(inntektskategoriV1);
        if (verdi == null) {
            throw new IllegalArgumentException("Ikke-støttet inntektskategori: " + inntektskategoriV1);
        }
        return verdi;
    }
}
