package no.nav.foreldrepenger.oppdrag.iverksett;

import static no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1.Inntektskategori.ARBEIDSTAKER;
import static no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1.Inntektskategori.FRILANSER;
import static no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1.SatsType.DAGSATS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseAndelV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseBehandlingInfoV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelsePeriodeV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1;

class TilkjentYtelseTestDataHelper {

    static final String AKTØR_ID = "90000123";
    static final String SAKSNUMMER = "2525253";
    static final String ANSVARLIG_SAKSBEHANDLER = "Z000001";
    static final long BEHANDLING_ID = 100000123L;
    static final LocalDate VEDTAKSDATO = LocalDate.of(2019, 3, 10);
    static final LocalDate ENDRINGSDATO = VEDTAKSDATO.plusDays(2);
    static final String ARBEIDSGIVER_ORG_NR = "123123123";

    private TilkjentYtelseTestDataHelper() {
    }

    static TilkjentYtelseV1 opprettTilkjentYtelseV1() {
        //TilkjentYtelseBehandlingInfoV1
        TilkjentYtelseBehandlingInfoV1 info = opprettTilkjentYtelseBehandlingInfoV1(TilkjentYtelseV1.YtelseType.FORELDREPENGER);

        //TilkjentYtelseAndelV1 - andeler1
        TilkjentYtelseAndelV1 andel_1_Bruker = TilkjentYtelseAndelV1.tilBruker(ARBEIDSTAKER, 100L, DAGSATS)
                .medArbeidsgiverOrgNr(ARBEIDSGIVER_ORG_NR);
        andel_1_Bruker.setUtbetalingsgrad(BigDecimal.valueOf(100));

        TilkjentYtelseAndelV1 andel_1_Arbeidsgiver = TilkjentYtelseAndelV1.refusjon(ARBEIDSTAKER, 1000L, DAGSATS)
                .medArbeidsgiverOrgNr(ARBEIDSGIVER_ORG_NR);
        andel_1_Arbeidsgiver.setUtbetalingsgrad(BigDecimal.valueOf(100));

        List<TilkjentYtelseAndelV1> andeler1 = Arrays.asList(andel_1_Bruker, andel_1_Arbeidsgiver);

        //TilkjentYtelseAndelV1 - andeler2
        TilkjentYtelseAndelV1 andel_2_Bruker = TilkjentYtelseAndelV1.tilBruker(FRILANSER, 135L, DAGSATS);
        andel_2_Bruker.setUtbetalingsgrad(BigDecimal.valueOf(100));

        TilkjentYtelseAndelV1 andel_2_Arbeidsgiver = TilkjentYtelseAndelV1.refusjon(ARBEIDSTAKER, 1586L, DAGSATS)
                .medFeriepenger(Year.of(2018), 187)
                .medArbeidsgiverOrgNr(ARBEIDSGIVER_ORG_NR);
        andel_2_Arbeidsgiver.setUtbetalingsgrad(BigDecimal.valueOf(100));

        List<TilkjentYtelseAndelV1> andeler2 = Arrays.asList(andel_2_Bruker, andel_2_Arbeidsgiver);

        //TilkjentYtelsePeriodeV1
        List<TilkjentYtelsePeriodeV1> perioder = new ArrayList<>();
        perioder.add(new TilkjentYtelsePeriodeV1(LocalDate.of(2018, 12, 24), LocalDate.of(2019, 2, 28), andeler1));
        perioder.add(new TilkjentYtelsePeriodeV1(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31), andeler2));

        return new TilkjentYtelseV1(info, perioder)
                .setEndringsdato(ENDRINGSDATO)
                .setErOpphør(false)
                .setErOpphørEtterSkjæringstidspunkt(false);
    }

    private static TilkjentYtelseBehandlingInfoV1 opprettTilkjentYtelseBehandlingInfoV1(TilkjentYtelseV1.YtelseType ytelseType) {
        return new TilkjentYtelseBehandlingInfoV1()
                .setAktørId(AKTØR_ID)
                .setSaksnummer(SAKSNUMMER)
                .setBehandlingId(BEHANDLING_ID)
                .setVedtaksdato(VEDTAKSDATO)
                .setGjelderAdopsjon(false)
                .setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER)
                .setYtelseType(ytelseType);
    }
}
