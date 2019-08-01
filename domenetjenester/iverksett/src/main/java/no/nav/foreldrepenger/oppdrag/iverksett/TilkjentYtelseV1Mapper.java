package no.nav.foreldrepenger.oppdrag.iverksett;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseAndelV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseBehandlingInfoV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseFeriepengerV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelsePeriodeV1;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseAndel;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseBehandlingInfo;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseFeriepenger;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelsePeriode;

class TilkjentYtelseV1Mapper {

    private TilkjentYtelseV1Mapper() {
        //hindrer instansiering, noe som gjør SonarQube glad
    }

    static TilkjentYtelseEntitet mapTilEntiteter(TilkjentYtelseV1 tilkjentYtelse) {
        TilkjentYtelseBehandlingInfoV1 infoV1 = tilkjentYtelse.getBehandingsinfo();
        TilkjentYtelseEntitet entitet = new TilkjentYtelseEntitet.Builder()
                .medBehandlingId(infoV1.getBehandlingId())
                .medEndringsdato(tilkjentYtelse.getEndringsdato())
                .medErOpphør(tilkjentYtelse.getErOpphør())
                .medErOpphørEtterStp(tilkjentYtelse.getErOpphørEtterSkjæringstidspunkt())
                .build();

        new TilkjentYtelseBehandlingInfo.Builder()
                .medSaksnummer(infoV1.getSaksnummer())
                .medAktørId(infoV1.getAktørId())
                .medYtelseType(map(infoV1.getYtelseType()))
                .medGjelderAdopsjon(infoV1.isGjelderAdopsjon())
                .medVedtaksdato(infoV1.getVedtaksdato())
                .medAnsvarligSaksbehandler(infoV1.getAnsvarligSaksbehandler())
                .medForrigeBehandlingId(infoV1.getForrigeBehandlingId())
                .build(entitet);

        for (TilkjentYtelsePeriodeV1 periodeV1 : tilkjentYtelse.getPerioder()) {
            TilkjentYtelsePeriode periodeEntitet = new TilkjentYtelsePeriode.Builder()
                    .medTilkjentYtelsePeriodeFomOgTom(periodeV1.getFom(), periodeV1.getTom())
                    .build(entitet);

            for (TilkjentYtelseAndelV1 andelV1 : periodeV1.getAndeler()) {
                TilkjentYtelseAndel andel = TilkjentYtelseAndel.builder()
                        .medUtbetalesTilBruker(andelV1.getUtbetalesTilBruker())
                        .medArbeidsgiverOrgNr(andelV1.getArbeidsgiverOrgNr())
                        .medArbeidsgiverAktørId(andelV1.getArbeidsgiverAktørId())
                        .medInntektskategori(InntektskategoriMapper.map(andelV1.getInntektskategori()))
                        .medSatsType(map(andelV1.getSatsType()))
                        .medSatsBeløp(andelV1.getSatsBeløp())
                        .medUtbetalingsgrad(andelV1.getUtbetalingsgrad())
                        .build(periodeEntitet);
                for (TilkjentYtelseFeriepengerV1 tyFeriepenger : andelV1.getFeriepenger()) {
                    new TilkjentYtelseFeriepenger.Builder()
                            .medOpptjeningsår(tyFeriepenger.getOpptjeningsår())
                            .medÅrsbeløp(tyFeriepenger.getBeløp())
                            .build(andel);
                }
            }
        }
        return entitet;
    }

    private static String map(TilkjentYtelseV1.YtelseType ytelseType) {
        //TODO bør bruke samme koder som i fpsak (FP/SVP/ES)
        switch (ytelseType) {
            case FORELDREPENGER:
                return "FORELDREPENGER";
            case SVANGERSKAPSPENGER:
                return "SVANGERSKAPSPENGER";
            case ENGANGSTØNAD:
                return "ENGANGSTØNAD";
        }
        throw new IllegalArgumentException("Ikke-støttet ytelseType: " + ytelseType);
    }

    private static String map(TilkjentYtelseV1.SatsType satsType) {
        if (TilkjentYtelseV1.SatsType.DAGSATS.equals(satsType)) {
            return "DAGSATS";
        } else if (TilkjentYtelseV1.SatsType.ENGANGSUTBETALING.equals(satsType)) {
            return "ENGANGSUTBETALING";
        } else {
            throw new IllegalArgumentException("Ikke-støttet sats-type: " + satsType);
        }
    }
}
