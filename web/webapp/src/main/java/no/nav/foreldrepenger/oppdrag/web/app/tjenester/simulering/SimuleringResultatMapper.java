package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.BeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Mottaker;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Oppsummering;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregning;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningPeriode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.DetaljertSimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.RadId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringForMottakerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerFagområdeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerMånedDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatRadDto;

class SimuleringResultatMapper {

    private HentNavnTjeneste hentNavnTjeneste;
    private DetaljertSimuleringResultatDto.Builder simuleringResultatBuilder;

    private SimuleringResultatMapper(HentNavnTjeneste hentNavnTjeneste) {
        Objects.requireNonNull(hentNavnTjeneste, "hentNavnTjeneste");
        this.hentNavnTjeneste = hentNavnTjeneste;
        this.simuleringResultatBuilder = new DetaljertSimuleringResultatDto.Builder();
    }

    public static SimuleringDto map(HentNavnTjeneste hentNavnTjeneste, SimulertBeregningResultat simulertBeregningResultat, boolean slåttAvInntrekk) {
        SimuleringResultatMapper mapper = new SimuleringResultatMapper(hentNavnTjeneste);
        DetaljertSimuleringResultatDto resultatDto = mapper.map(simulertBeregningResultat.getBeregningResultat(), simulertBeregningResultat.getGjelderYtelseType());

        mapper.simuleringResultatBuilder = new DetaljertSimuleringResultatDto.Builder();
        Optional<DetaljertSimuleringResultatDto> resultatDtoUtenInntrekk = simulertBeregningResultat.getBeregningResultatUtenInntrekk()
                .map(b -> mapper.map(b, simulertBeregningResultat.getGjelderYtelseType()));

        return resultatDtoUtenInntrekk.map(simuleringResultatDto -> new SimuleringDto(resultatDto, simuleringResultatDto)).orElseGet(() -> new SimuleringDto(resultatDto, slåttAvInntrekk));
    }

    private DetaljertSimuleringResultatDto map(BeregningResultat beregningsresultat, YtelseType ytelseType) {
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker = beregningsresultat.getBeregningPerMottaker();
        for (var entry : beregningPerMottaker.entrySet()) {
            Mottaker mottaker = entry.getKey();
            List<SimulertBeregningPeriode> perioder = entry.getValue();
            if (MottakerType.BRUKER.equals(mottaker.getMottakerType())) {
                simuleringResultatBuilder.medIngenPerioderMedAvvik(harIngenPerioderMedAvvik(mottaker, perioder));
            }
            leggTilBeregnetResultat(mottaker, perioder, ytelseType);
        }
        leggTilOppsummertResultat(beregningsresultat.getOppsummering());
        return buildDto();
    }

    private boolean harIngenPerioderMedAvvik(Mottaker mottaker, List<SimulertBeregningPeriode> perioder) {
        YearMonth nesteUtbetalingsperiode = YearMonth.from(mottaker.getNesteUtbetalingsperiodeFom());
        boolean harTidligereUtbetalingsperiode = perioder.stream()
                .anyMatch(p -> !YearMonth.from(p.getPeriode().getPeriodeFom()).equals(nesteUtbetalingsperiode));
        return !harTidligereUtbetalingsperiode;
    }

    private void leggTilBeregnetResultat(Mottaker mottaker, List<SimulertBeregningPeriode> beregnetRestulat, YtelseType ytelseType) {
        SimuleringForMottakerDto mottakerDto = mapMottaker(mottaker, beregnetRestulat, ytelseType);
        simuleringResultatBuilder.medPerioderForMottaker(mottakerDto);
    }

    private void leggTilOppsummertResultat(Oppsummering oppsummering) {
        simuleringResultatBuilder.medInntrekk(oppsummering.getInntrekkNesteUtbetaling())
                .medSumFeilutbetaling(oppsummering.getFeilutbetaling())
                .medSumEtterbetaling(oppsummering.getEtterbetaling())
                .medPeriode(oppsummering.getPeriodeFom(), oppsummering.getPeriodeTom());
    }

    private DetaljertSimuleringResultatDto buildDto() {
        return simuleringResultatBuilder.build();
    }

    private SimuleringForMottakerDto mapMottaker(Mottaker mottaker, List<SimulertBeregningPeriode> simulertBeregningPerioder, YtelseType ytelseType) {
        MottakerType mottakerType = mottaker.getMottakerType();
        String mottakerNummer = mottaker.getMottakerNummer();

        var builder = new SimuleringForMottakerDto.Builder()
                .medGjelderYtelseType(ytelseType)
                .medMottakerType(mottakerType)
                .medResultatPerFagområde(mapPerFagområde(simulertBeregningPerioder));

        if (!ytelseType.gjelderEngangsstønad()) {
            builder.medNesteUtbetalingsperiode(new Periode(mottaker.getNesteUtbetalingsperiodeFom(), mottaker.getNesteUtbetalingsperiodeTom()));
        }

        if (MottakerType.BRUKER.equals(mottakerType)) {
            if (!ytelseType.gjelderEngangsstønad()) {
                builder.medResultatOgMotregningRader(mapResultaterPerMåned(simulertBeregningPerioder));

            }
            return builder.build();
        }
        if (MottakerType.ARBG_PRIV.equals(mottakerType)) {
            String navn = hentNavnTjeneste.hentNavnGittFnr(mottakerNummer);
            return builder
                    .medMottakerNavn(navn)
                    .medMottakerNummer(mottakerNummer)
                    .build();
        }
        if (MottakerType.ARBG_ORG.equals(mottakerType)) {
            String orgNavn = hentNavnTjeneste.hentNavnGittOrgnummer(mottakerNummer);
            return builder.medMottakerNummer(mottakerNummer)
                    .medMottakerNavn(orgNavn)
                    .build();
        }

        throw new IllegalArgumentException("Ukjent mottaker-mottakerType: " + mottakerType);
    }


    private List<SimuleringResultatRadDto> mapResultaterPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        if (erFlereFagområder(simulertBeregningPerioder)) {
            return Arrays.asList(
                    mapResultatMotregningPerMåned(simulertBeregningPerioder),
                    mapInntrekkPerMåned(simulertBeregningPerioder),
                    mapResultatPerMåned(simulertBeregningPerioder));
        } else {
            return Arrays.asList(
                    mapInntrekkPerMåned(simulertBeregningPerioder),
                    mapResultatPerMåned(simulertBeregningPerioder));
        }
    }

    private SimuleringResultatRadDto mapResultatMotregningPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getResultatEtterMotregning, RadId.RESULTAT_ETTER_MOTREGNING);
    }

    private SimuleringResultatRadDto mapInntrekkPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getInntrekkNesteMåned, RadId.INNTREKK_NESTE_MÅNED);
    }

    private SimuleringResultatRadDto mapResultatPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getResultat, RadId.RESULTAT);
    }

    private SimuleringResultatRadDto mapPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder, Function<SimulertBeregningPeriode, BigDecimal> hva, RadId feltnavn) {
        SimuleringResultatRadDto.Builder resultatRadDto = new SimuleringResultatRadDto.Builder().medFeltnavn(feltnavn);
        for (SimulertBeregningPeriode sbp : simulertBeregningPerioder) {
            resultatRadDto.medResultatPerMåned(new SimuleringResultatPerMånedDto(sbp.getPeriode(), hva.apply(sbp)));
        }
        return resultatRadDto.build();
    }

    private boolean erFlereFagområder(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return finnUnikeFagområder(simulertBeregningPerioder).size() > 1;
    }

    private Set<FagOmrådeKode> finnUnikeFagområder(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return simulertBeregningPerioder
                .stream()
                .map(p -> p.getBeregningPerFagområde().keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }


    private List<SimuleringResultatPerFagområdeDto> mapPerFagområde(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        var perFagområde = mapFelterPerFagområdePerMåned(simulertBeregningPerioder);

        List<SimuleringResultatPerFagområdeDto> resultat = new ArrayList<>();
        for (var entry : perFagområde.entrySet()) {
            List<SimuleringResultatRadDto> rader = mapForFagområde(entry.getValue());
            resultat.add(new SimuleringResultatPerFagområdeDto.Builder()
                    .medFagområdeKode(entry.getKey())
                    .medRader(rader)
                    .build());
        }
        return resultat;
    }

    private List<SimuleringResultatRadDto> mapForFagområde(Map<RadId, List<SimuleringResultatPerMånedDto>> radDtoEr) {
        List<SimuleringResultatRadDto> rader = new ArrayList<>();
        boolean harTidligereUtbetaltBeløp = harTidligereUtbetaltBeløp(radDtoEr);

        for (var entryRad : radDtoEr.entrySet()) {
            RadId feltnavn = entryRad.getKey();
            if (skalViseFeltForFagområde(feltnavn, harTidligereUtbetaltBeløp)) {
                rader.add(new SimuleringResultatRadDto.Builder()
                        .medFeltnavn(feltnavn)
                        .medResultaterPerMåned(entryRad.getValue())
                        .build());
            }
        }
        return rader;
    }

    private boolean harTidligereUtbetaltBeløp(Map<RadId, List<SimuleringResultatPerMånedDto>> rader) {
        return rader.get(RadId.TIDLIGERE_UTBETALT)
                .stream().anyMatch(p -> p.getBeløp() > 0);
    }

    private Map<FagOmrådeKode, Map<RadId, List<SimuleringResultatPerMånedDto>>> mapFelterPerFagområdePerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        Map<FagOmrådeKode, Map<RadId, List<SimuleringResultatPerMånedDto>>> perFagområde = lagTommeResultatRader(simulertBeregningPerioder);

        for (SimulertBeregningPeriode sbp : simulertBeregningPerioder) {
            Periode periode = sbp.getPeriode();
            for (Map.Entry<FagOmrådeKode, SimulertBeregning> entry : sbp.getBeregningPerFagområde().entrySet()) {
                FagOmrådeKode fagOmråde = entry.getKey();
                SimulertBeregning beregning = entry.getValue();
                Map<RadId, List<SimuleringResultatPerMånedDto>> resultatPerFelt = perFagområde.get(fagOmråde);
                leggTil(resultatPerFelt, RadId.NYTT_BELØP, periode, beregning.getNyttBeregnetBeløp());
                leggTil(resultatPerFelt, RadId.TIDLIGERE_UTBETALT, periode, beregning.getTidligereUtbetaltBeløp());
                leggTil(resultatPerFelt, RadId.DIFFERANSE, periode, beregning.getDifferanse());
            }
        }
        return perFagområde;
    }

    private Map<FagOmrådeKode, Map<RadId, List<SimuleringResultatPerMånedDto>>> lagTommeResultatRader(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return simulertBeregningPerioder.stream()
                .flatMap(sbp -> sbp.getBeregningPerFagområde().keySet().stream())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), f -> lagTommeResultatRader()));
    }

    private static void leggTil(Map<RadId, List<SimuleringResultatPerMånedDto>> resultatPerFelt, RadId feltnavn, Periode periode, BigDecimal verdi) {
        resultatPerFelt.get(feltnavn).add(new SimuleringResultatPerMånedDto(periode, verdi));
    }

    private boolean skalViseFeltForFagområde(RadId feltnavn, boolean harTidligereUtbetaltBeløp) {
        return RadId.NYTT_BELØP == feltnavn || harTidligereUtbetaltBeløp;
    }

    private Map<RadId, List<SimuleringResultatPerMånedDto>> lagTommeResultatRader() {
        Map<RadId, List<SimuleringResultatPerMånedDto>> resultatPerFelt = new EnumMap<>(RadId.class);
        resultatPerFelt.put(RadId.NYTT_BELØP, new ArrayList<>());
        resultatPerFelt.put(RadId.TIDLIGERE_UTBETALT, new ArrayList<>());
        resultatPerFelt.put(RadId.DIFFERANSE, new ArrayList<>());
        return resultatPerFelt;
    }
}
