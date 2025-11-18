package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.RadId;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.PeriodeDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.BeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Mottaker;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Oppsummering;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningPeriode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

class SimuleringResultatMapper {

    private final PersonTjeneste personTjeneste;
    private DetaljertSimuleringResultatDtoBuilder simuleringResultatBuilder;

    private SimuleringResultatMapper(PersonTjeneste personTjeneste) {
        Objects.requireNonNull(personTjeneste, "hentNavnTjeneste");
        this.personTjeneste = personTjeneste;
        this.simuleringResultatBuilder = new DetaljertSimuleringResultatDtoBuilder();
    }

    public static SimuleringDto map(PersonTjeneste personTjeneste, SimulertBeregningResultat simulertBeregningResultat, boolean slåttAvInntrekk) {
        var mapper = new SimuleringResultatMapper(personTjeneste);
        var resultatDto = mapper.map(simulertBeregningResultat.getBeregningResultat(), simulertBeregningResultat.getGjelderYtelseType());

        mapper.simuleringResultatBuilder = new DetaljertSimuleringResultatDtoBuilder();
        var resultatDtoUtenInntrekk = simulertBeregningResultat.getBeregningResultatUtenInntrekk()
                .map(b -> mapper.map(b, simulertBeregningResultat.getGjelderYtelseType()));

        return resultatDtoUtenInntrekk.map(simuleringResultatDto -> new SimuleringDto(resultatDto, simuleringResultatDto, false))
            .orElseGet(() -> new SimuleringDto(resultatDto, null, slåttAvInntrekk));
    }

    private SimuleringDto.DetaljertSimuleringResultatDto map(BeregningResultat beregningsresultat, YtelseType ytelseType) {
        var beregningPerMottaker = beregningsresultat.getBeregningPerMottaker();
        for (var entry : beregningPerMottaker.entrySet()) {
            var mottaker = entry.getKey();
            var perioder = entry.getValue();
            if (MottakerType.BRUKER.equals(mottaker.mottakerType())) {
                simuleringResultatBuilder.medIngenPerioderMedAvvik(harIngenPerioderMedAvvik(mottaker, perioder));
            }
            leggTilBeregnetResultat(mottaker, perioder, ytelseType);
        }
        leggTilOppsummertResultat(beregningsresultat.getOppsummering());
        return buildDto();
    }

    private boolean harIngenPerioderMedAvvik(Mottaker mottaker, List<SimulertBeregningPeriode> perioder) {
        var nesteUtbetalingsperiode = YearMonth.from(mottaker.nesteUtbetalingsperiodeFom());
        var harTidligereUtbetalingsperiode = perioder.stream()
                .anyMatch(p -> !YearMonth.from(p.getPeriode().getPeriodeFom()).equals(nesteUtbetalingsperiode));
        return !harTidligereUtbetalingsperiode;
    }

    private void leggTilBeregnetResultat(Mottaker mottaker, List<SimulertBeregningPeriode> beregnetRestulat, YtelseType ytelseType) {
        var mottakerDto = mapMottaker(mottaker, beregnetRestulat, ytelseType);
        simuleringResultatBuilder.medPerioderForMottaker(mottakerDto);
    }

    private void leggTilOppsummertResultat(Oppsummering oppsummering) {
        simuleringResultatBuilder.medInntrekk(oppsummering.getInntrekkNesteUtbetaling())
                .medSumFeilutbetaling(oppsummering.getFeilutbetaling())
                .medSumEtterbetaling(oppsummering.getEtterbetaling())
                .medPeriode(oppsummering.getPeriodeFom(), oppsummering.getPeriodeTom());
    }

    private SimuleringDto.DetaljertSimuleringResultatDto buildDto() {
        return simuleringResultatBuilder.build();
    }

    private SimuleringDto.SimuleringForMottakerDto mapMottaker(Mottaker mottaker, List<SimulertBeregningPeriode> simulertBeregningPerioder, YtelseType ytelseType) {
        var mottakerType = mottaker.mottakerType();
        var mottakerNummer = mottaker.mottakerNummer();

        var builder = new SimuleringForMottakerDtoBuilder()
                .medGjelderYtelseType(ytelseType)
                .medMottakerType(mottakerType)
                .medResultatPerFagområde(mapPerFagområde(simulertBeregningPerioder));

        if (ytelseType.erIkkeEngangsstønad()) {
            builder.medNesteUtbetalingsperiode(mottaker.nesteUtbetalingsperiodeFom(), mottaker.nesteUtbetalingsperiodeTom());
        }

        if (MottakerType.BRUKER.equals(mottakerType)) {
            if (ytelseType.erIkkeEngangsstønad()) {
                builder.medResultatOgMotregningRader(mapResultaterPerMåned(simulertBeregningPerioder));

            }
            return builder.build();
        }
        if (MottakerType.ARBG_PRIV.equals(mottakerType)) {
            return builder
                    .medMottakerNummer(mottakerNummerTilFNR(mottakerNummer))
                    .medMottakerIdentifikator(mottakerNummerTilAktørId(mottakerNummer))
                    .build();
        }
        if (MottakerType.ARBG_ORG.equals(mottakerType)) {
            return builder.medMottakerNummer(mottakerNummer)
                    .medMottakerIdentifikator(mottakerNummer)
                    .build();
        }

        throw new IllegalArgumentException("Ukjent mottaker-mottakerType: " + mottakerType);
    }

    private String mottakerNummerTilFNR(String mottakerNummer) {
        return AktørId.erGyldigAktørId(mottakerNummer) ? personTjeneste.hentFnrForAktørId(new AktørId(mottakerNummer)).orElse(mottakerNummer) : mottakerNummer;
    }

    private String mottakerNummerTilAktørId(String mottakerNummer) {
        return AktørId.erGyldigAktørId(mottakerNummer) ? mottakerNummer : personTjeneste.hentAktørForFnr(mottakerNummer).map(AktørId::getId).orElse(mottakerNummer);
    }

    private List<SimuleringDto.SimuleringResultatRadDto> mapResultaterPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
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

    private SimuleringDto.SimuleringResultatRadDto mapResultatMotregningPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getResultatEtterMotregning, RadId.RESULTAT_ETTER_MOTREGNING);
    }

    private SimuleringDto.SimuleringResultatRadDto mapInntrekkPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getInntrekkNesteMåned, RadId.INNTREKK_NESTE_MÅNED);
    }

    private SimuleringDto.SimuleringResultatRadDto mapResultatPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return mapPerMåned(simulertBeregningPerioder, SimulertBeregningPeriode::getResultat, RadId.RESULTAT);
    }

    private SimuleringDto.SimuleringResultatRadDto mapPerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder, Function<SimulertBeregningPeriode, BigDecimal> hva, RadId feltnavn) {
        var månedsresultater = simulertBeregningPerioder.stream()
            .map(sbp -> lagResultatPerMånedDto(sbp.getPeriode(), hva.apply(sbp)))
            .toList();
        return lagRadDtoMedSortertePerioder(feltnavn, månedsresultater);
    }

    private boolean erFlereFagområder(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return simulertBeregningPerioder
                .stream()
                .map(p -> p.getBeregningPerFagområde().keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .size() > 1;
    }


    private List<SimuleringDto.SimuleringResultatPerFagområdeDto> mapPerFagområde(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        var perFagområde = mapFelterPerFagområdePerMåned(simulertBeregningPerioder);

        List<SimuleringDto.SimuleringResultatPerFagområdeDto> resultat = new ArrayList<>();
        for (var entry : perFagområde.entrySet()) {
            var rader = mapForFagområde(entry.getValue()).stream()
                .sorted(Comparator.comparingInt(o -> o.feltnavn().ordinal()))
                .toList();
            resultat.add(new SimuleringDto.SimuleringResultatPerFagområdeDto(entry.getKey(), rader));
        }
        return resultat;
    }

    private List<SimuleringDto.SimuleringResultatRadDto> mapForFagområde(Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>> radDtoEr) {
        List<SimuleringDto.SimuleringResultatRadDto> rader = new ArrayList<>();
        var harTidligereUtbetaltBeløp = harTidligereUtbetaltBeløp(radDtoEr);

        for (var entryRad : radDtoEr.entrySet()) {
            var feltnavn = entryRad.getKey();
            if (skalViseFeltForFagområde(feltnavn, harTidligereUtbetaltBeløp)) {
                rader.add(lagRadDtoMedSortertePerioder(feltnavn, entryRad.getValue()));
            }
        }
        return rader;
    }

    private boolean harTidligereUtbetaltBeløp(Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>> rader) {
        return rader.get(RadId.TIDLIGERE_UTBETALT)
                .stream().anyMatch(p -> p.beløp() > 0);
    }

    private Map<Fagområde, Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>>> mapFelterPerFagområdePerMåned(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        var perFagområde = lagTommeResultatRader(simulertBeregningPerioder);

        for (var sbp : simulertBeregningPerioder) {
            var periode = sbp.getPeriode();
            for (var entry : sbp.getBeregningPerFagområde().entrySet()) {
                var fagOmråde = Fagområde.valueOf(entry.getKey().name());
                var beregning = entry.getValue();
                var resultatPerFelt = perFagområde.get(fagOmråde);
                leggTil(resultatPerFelt, RadId.NYTT_BELØP, periode, beregning.getNyttBeregnetBeløp());
                leggTil(resultatPerFelt, RadId.TIDLIGERE_UTBETALT, periode, beregning.getTidligereUtbetaltBeløp());
                leggTil(resultatPerFelt, RadId.DIFFERANSE, periode, beregning.getDifferanse());
            }
        }
        return perFagområde;
    }

    private Map<Fagområde, Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>>> lagTommeResultatRader(List<SimulertBeregningPeriode> simulertBeregningPerioder) {
        return simulertBeregningPerioder.stream()
                .flatMap(sbp -> sbp.getBeregningPerFagområde().keySet().stream())
                .map(fo -> Fagområde.valueOf(fo.name()))
                .distinct()
                .collect(Collectors.toMap(Function.identity(), f -> lagTommeResultatRader()));
    }

    private static void leggTil(Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>> resultatPerFelt, RadId feltnavn, Periode periode, BigDecimal verdi) {
        resultatPerFelt.get(feltnavn).add(lagResultatPerMånedDto(periode, verdi));
    }

    private static SimuleringDto.SimuleringResultatPerMånedDto lagResultatPerMånedDto(Periode periode, BigDecimal verdi) {
        return new SimuleringDto.SimuleringResultatPerMånedDto(new PeriodeDto(periode.getPeriodeFom(), periode.getPeriodeTom()),
            verdi != null ? verdi.longValue() : null);
    }

    private boolean skalViseFeltForFagområde(RadId feltnavn, boolean harTidligereUtbetaltBeløp) {
        return RadId.NYTT_BELØP == feltnavn || harTidligereUtbetaltBeløp;
    }

    private Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>> lagTommeResultatRader() {
        Map<RadId, List<SimuleringDto.SimuleringResultatPerMånedDto>> resultatPerFelt = new EnumMap<>(RadId.class);
        resultatPerFelt.put(RadId.NYTT_BELØP, new ArrayList<>());
        resultatPerFelt.put(RadId.TIDLIGERE_UTBETALT, new ArrayList<>());
        resultatPerFelt.put(RadId.DIFFERANSE, new ArrayList<>());
        return resultatPerFelt;
    }

    private static SimuleringDto.SimuleringResultatRadDto lagRadDtoMedSortertePerioder(RadId feltnavn, List<SimuleringDto.SimuleringResultatPerMånedDto> resultatPerMåned) {
        return new SimuleringDto.SimuleringResultatRadDto(feltnavn, resultatPerMåned.stream().sorted(Comparator.comparing(rpm -> rpm.periode().fom())).toList());
    }
}
