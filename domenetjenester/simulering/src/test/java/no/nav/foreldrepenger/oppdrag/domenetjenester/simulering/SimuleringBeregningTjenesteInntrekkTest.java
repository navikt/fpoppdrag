package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.D;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.K;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.SP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEIL;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.JUST;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.SKAT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

class SimuleringBeregningTjenesteInntrekkTest {

    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();

    private Periode april = new Periode(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
    private Periode mai = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 31));
    private Periode juni = new Periode(LocalDate.of(2019, 6, 1), LocalDate.of(2019, 6, 30));

    @Test
    void skal_regne_inntrekk_over_alle_utbetalingsperioder_når_toggle_er_på() {

        var mottaker = new Mottaker(MottakerType.BRUKER, null, juni);

        List<SimulertBeregningPeriode> inntrekkJuni = Arrays.asList(
                lagPeriodeMedMotregning(juni, -10000),
                lagPeriodeMedMotregning(mai, 10000)
        );
        List<SimulertBeregningPeriode> inntrekkMai = Arrays.asList(
                lagPeriodeMedMotregning(mai, -10000),
                lagPeriodeMedMotregning(april, 10000)
        );

        assertThat(beregnInntrekk(mottaker, inntrekkJuni)).isEqualByComparingTo(BigDecimal.valueOf(-10000));
        assertThat(beregnInntrekk(mottaker, inntrekkMai)).isEqualByComparingTo(BigDecimal.valueOf(-10000));

    }

    private BigDecimal beregnInntrekk(Mottaker mottaker, List<SimulertBeregningPeriode> perioder) {
        return simuleringBeregningTjeneste.finnInntrekk(Map.of(mottaker, perioder), Fagområde.FP);
    }

    private SimulertBeregningPeriode lagPeriodeMedMotregning(Periode periode, int motregning) {
        return SimulertBeregningPeriode.builder()
                .medPeriode(periode)
                .medBeregning(FP, SimulertBeregning.builder().medMotregning(BigDecimal.valueOf(motregning)).build())
                .build();
    }

    @Test
    void skal_ha_at_tidligere_utbetalt_beløp_er_sum_av_kreditposter_for_ytelse() {

        // Act
        BigDecimal tidligereUtbetaltBeløp = SimuleringBeregningTjeneste.beregnTidligereUtbetaltBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FP, YTEL, K, 3000),
                postering("16.09.2018-30.09.2018", FP, YTEL, K, 2000),
                postering("16.09.2018-30.09.2018", FP, YTEL, D, 1500),
                postering("01.09.2018-30.09.2018", FP, SKAT, K, 100),
                postering("01.09.2018-30.09.2018", FP, SKAT, D, 200)));

        //Assert
        assertThat(tidligereUtbetaltBeløp).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    void skal_ha_at_nytt_beløp_er_sum_av_debetposter_for_ytelse() {
        // Act
        BigDecimal nyttBeløp = SimuleringBeregningTjeneste.beregnNyttBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FP, YTEL, K, 3000),
                postering("16.09.2018-30.09.2018", FP, YTEL, K, 2000),
                postering("16.09.2018-30.09.2018", FP, YTEL, D, 1500),
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000),
                postering("01.09.2018-30.09.2018", FP, SKAT, K, 100),
                postering("01.09.2018-30.09.2018", FP, SKAT, D, 200)));

        // Assert
        assertThat(nyttBeløp).isEqualTo(BigDecimal.valueOf(2500));
    }

    @Test
    void skal_ha_at_feilutbetalt_beløp_er_sum_av_feilutbetaling_poster() {

        // Act
        BigDecimal feilutbetaltBeløp = SimuleringBeregningTjeneste.beregnFeilutbetaltBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000),
                postering("16.09.2018-30.09.2018", FP, YTEL, D, 1500),
                postering("01.09.2018-30.09.2018", FP, SKAT, K, 100),
                postering("01.09.2018-15.09.2018", FP, FEIL, D, 100),
                postering("16.09.2018-30.09.2018", FP, FEIL, D, 200)));

        // Assert
        assertThat(feilutbetaltBeløp).isEqualTo(BigDecimal.valueOf(300));
    }

    @Test
    void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_etterbetaling() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("16.09.2018-30.09.2018", FP, YTEL, K, 2000),
                postering("16.09.2018-30.09.2018", FP, YTEL, D, 1500),
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000),
                postering("01.09.2018-30.09.2018", FP, SKAT, K, 100),
                postering("01.09.2018-30.09.2018", FP, SKAT, D, 200)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode periode = simulertBeregningPerioder.get(0);
        assertThat(periode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(periode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2018, 9, 30));

        assertThat(periode.getBeregningPerFagområde()).containsOnlyKeys(FP);
        SimulertBeregning simulertBeregning = periode.getBeregningPerFagområde().get(FP);
        assertThat(simulertBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(simulertBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(simulertBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getResultat()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
        assertThat(simulertBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_feilutbetaling() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("01.09.2017-30.09.2017", FP, YTEL, D, 8928),
                postering("06.09.2017-30.09.2017", FP, YTEL, D, 5958),
                postering("06.09.2017-30.09.2017", FP, YTEL, K, 14886),
                postering("06.09.2017-30.09.2017", FP, FEIL, D, 8928)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode periode = simulertBeregningPerioder.get(0);
        assertThat(periode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2017, 9, 1));
        assertThat(periode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2017, 9, 30));

        assertThat(periode.getBeregningPerFagområde()).containsOnlyKeys(FP);
        SimulertBeregning simulertBeregning = periode.getBeregningPerFagområde().get(FP);
        assertThat(simulertBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(simulertBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(5958));
        assertThat(simulertBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getResultat()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
        assertThat(simulertBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_sykepenger_og_foreldrepenger() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("01.09.2018-30.09.2018", SP, YTEL, D, 4000),
                postering("01.09.2018-30.09.2018", SP, YTEL, K, 3000),
                postering("16.09.2018-30.09.2018", FP, YTEL, K, 2000),
                postering("16.09.2018-30.09.2018", FP, YTEL, D, 1500),
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode simulertBeregningPeriode = simulertBeregningPerioder.get(0);
        assertThat(simulertBeregningPeriode.getBeregningPerFagområde()).containsOnlyKeys(FP, SP);
        assertThat(simulertBeregningPeriode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(simulertBeregningPeriode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2018, 9, 30));

        // Assert - foreldrepenger
        SimulertBeregning foreldrepengerBeregning = simulertBeregningPeriode.getBeregningPerFagområde().get(FP);
        assertThat(foreldrepengerBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(foreldrepengerBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(foreldrepengerBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(foreldrepengerBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(foreldrepengerBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepengerBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);

        // Assert - sykepenger
        SimulertBeregning sykepengerBeregning = simulertBeregningPeriode.getBeregningPerFagområde().get(SP);
        assertThat(sykepengerBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(sykepengerBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(sykepengerBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(sykepengerBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(sykepengerBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepengerBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void skal_summere_justeringskontoer() {
        BigDecimal resultat = SimuleringBeregningTjeneste.beregnMotregning(Arrays.asList(
                postering("16.09.2018-30.09.2018", FP, JUST, D, 1000),
                postering("16.09.2018-30.09.2018", FP, JUST, K, 500),
                postering("01.09.2018-15.09.2018", FP, YTEL, K, 3000),
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000)));

        assertThat(resultat).isEqualTo(BigDecimal.valueOf(500));

        // Tom liste skal gi sum 0
        assertThat(SimuleringBeregningTjeneste.beregnMotregning(Collections.emptyList())).isEqualTo(BigDecimal.ZERO);

        // Ingen justeringsposter skal gi sum 0
        assertThat(SimuleringBeregningTjeneste.beregnMotregning(Arrays.asList(postering("01.09.2018-15.09.2018", FP, YTEL, K, 3000),
                postering("01.09.2018-15.09.2018", FP, YTEL, D, 1000)))).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void oppretterOppsummeringForForeldrepenger() {
        // Arrange
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();
        BigDecimal feilutbetaltBeløp = BigDecimal.valueOf(-5000);
        BigDecimal inntrekkNesteMåned = BigDecimal.valueOf(-3000);
        BigDecimal etterbetaling = BigDecimal.valueOf(4000);
        var mottaker = new Mottaker(MottakerType.BRUKER, "12345",
            LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30));

        SimulertBeregningPeriode september = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 9, 1), LocalDate.of(2018, 9, 30)))
                .medBeregning(Fagområde.FP, SimulertBeregning.builder()
                        .medEtterbetaling(etterbetaling)
                        .build())
                .build();

        SimulertBeregningPeriode oktober = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 31)))
                .medBeregning(Fagområde.FP, SimulertBeregning.builder()
                        .medFeilutbetaltBeløp(feilutbetaltBeløp)
                        .build())
                .build();

        SimulertBeregningPeriode november = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30)))
                .medBeregning(Fagområde.FP, SimulertBeregning.builder()
                        .medMotregning(inntrekkNesteMåned)
                        .build())
                .build();

        beregningsresultat.put(mottaker, Arrays.asList(september, oktober, november));

        // Act
        Oppsummering oppsummering = simuleringBeregningTjeneste.opprettOppsummering(beregningsresultat, YtelseType.FP);

        // Assert
        assertThat(oppsummering.getEtterbetaling()).isEqualTo(etterbetaling);
        assertThat(oppsummering.getFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
        assertThat(oppsummering.getInntrekkNesteUtbetaling()).isEqualTo(inntrekkNesteMåned);
        assertThat(oppsummering.getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(oppsummering.getPeriodeTom()).isEqualTo(LocalDate.of(2018, 10, 31));
    }

    @Test
    void oppretterOppsummeringForEngangsstønad() {
        // Arrange
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();
        BigDecimal feilutbetaltBeløp = BigDecimal.valueOf(-40000);
        var mottaker = new Mottaker(MottakerType.BRUKER, "12345",
            LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30));

        SimulertBeregningPeriode simulertBeregningPeriode = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 31)))
                .medBeregning(Fagområde.REFUTG, SimulertBeregning.builder()
                        .medFeilutbetaltBeløp(feilutbetaltBeløp)
                        .build())
                .build();
        beregningsresultat.put(mottaker, Collections.singletonList(simulertBeregningPeriode));

        // Act
        Oppsummering oppsummering = simuleringBeregningTjeneste.opprettOppsummering(beregningsresultat, YtelseType.ES);

        // Assert
        assertThat(oppsummering.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
        assertThat(oppsummering.getFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
        assertThat(oppsummering.getInntrekkNesteUtbetaling()).isNull();
        assertThat(oppsummering.getPeriodeFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(oppsummering.getPeriodeTom()).isEqualTo(LocalDate.of(2018, 10, 31));
    }

    @Test
    void skal_beregne_inntrekk_og_feilutbetaling_scenario_over_to_måneder() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for juni, feilutbetaling og inntrekk fra neste måned
                postering("01.06.2017-19.06.2017", FP, YTEL, D, 14952),
                postering("01.06.2017-30.06.2017", FP, JUST, D, 10680),
                postering("01.06.2017-30.06.2017", FP, JUST, D, 10680),
                postering("01.06.2017-30.06.2017", FP, YTEL, K, 46992),
                postering("24.06.2017-30.06.2017", FP, FEIL, D, 10680),
                postering("24.06.2017-30.06.2017", FP, YTEL, D, 10680),
                postering("01.06.2017-30.06.2017", FP, JUST, K, 10680),
                postering("19.06.2017-23.06.2017", FP, YTEL, D, 10680),

                //Posteringer for juli, med inntrekk
                postering("01.07.2017-31.07.2017", FP, JUST, K, 10680),
                postering("03.07.2017-31.07.2017", FP, YTEL, D, 44856)
        ));

        // Assert
        assertThat(resultat).hasSize(2);
        resultat.sort(Comparator.comparing(p -> p.getPeriode().getPeriodeFom()));
        // Sjekker resultat for juni
        SimulertBeregningPeriode resultatJuni = resultat.get(0);
        assertThat(resultatJuni.getResultatEtterMotregning()).isEqualTo(BigDecimal.valueOf(-21360));
        assertThat(resultatJuni.getInntrekkNesteMåned()).isEqualTo(BigDecimal.valueOf(10680));
        assertThat(resultatJuni.getResultat()).isEqualTo(BigDecimal.valueOf(-10680));

        assertThat(resultatJuni.getBeregningPerFagområde().keySet()).hasSize(1);
        SimulertBeregning foreldrepengerJuni = resultatJuni.getBeregningPerFagområde().get(FP);
        assertThat(foreldrepengerJuni.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(46992));
        assertThat(foreldrepengerJuni.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(25632));
        assertThat(foreldrepengerJuni.getDifferanse()).isEqualTo(BigDecimal.valueOf(-21360));
        assertThat(foreldrepengerJuni.getResultat()).isEqualTo(BigDecimal.valueOf(-10680));
        assertThat(foreldrepengerJuni.getMotregning()).isEqualTo(BigDecimal.valueOf(10680));
        assertThat(foreldrepengerJuni.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.valueOf(-10680));

        // Sjekker resultat for juli
        SimulertBeregningPeriode resultatJuli = resultat.get(1);
        assertThat(resultatJuli.getResultatEtterMotregning()).isEqualTo(BigDecimal.valueOf(44856));
        assertThat(resultatJuli.getInntrekkNesteMåned()).isEqualTo(BigDecimal.valueOf(-10680));
        assertThat(resultatJuli.getResultat()).isEqualTo(BigDecimal.valueOf(34176));

        assertThat(resultatJuli.getBeregningPerFagområde().keySet()).hasSize(1);
        SimulertBeregning foreldrepengerJuli = resultatJuli.getBeregningPerFagområde().get(FP);
        assertThat(foreldrepengerJuli.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepengerJuli.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(44856));
        assertThat(foreldrepengerJuli.getDifferanse()).isEqualTo(BigDecimal.valueOf(44856));
        assertThat(foreldrepengerJuli.getResultat()).isEqualTo(BigDecimal.valueOf(34176));
        assertThat(foreldrepengerJuli.getMotregning()).isEqualTo(BigDecimal.valueOf(-10680));
        assertThat(foreldrepengerJuli.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void skal_beregne_motregning_mellom_to_ytelser_innenfor_samme_måned() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for foreldrepenger
                postering("01.09.2017-30.09.2017", FP, SKAT, K, 5029),
                postering("01.09.2017-30.09.2017", FP, JUST, K, 517),
                postering("06.09.2017-30.09.2017", FP, YTEL, D, 14886),

                // Posteringer for sykepenger
                postering("01.09.2017-05.09.2017", SP, YTEL, D, 1551),
                postering("01.09.2017-06.09.2017", SP, YTEL, K, 2068),
                postering("01.09.2017-30.09.2017", SP, JUST, D, 517)
        ));

        // Assert
        assertThat(resultat).hasSize(1);

        SimulertBeregningPeriode periode = resultat.get(0);
        assertThat(periode.getResultatEtterMotregning()).isEqualTo(BigDecimal.valueOf(14369));
        assertThat(periode.getInntrekkNesteMåned()).isEqualTo(BigDecimal.ZERO);
        assertThat(periode.getResultat()).isEqualTo(BigDecimal.valueOf(14369));

        assertThat(periode.getBeregningPerFagområde().keySet()).hasSize(2);

        // Sjekker resultat for foreldrepenger
        SimulertBeregning foreldrepenger = periode.getBeregningPerFagområde().get(FP);
        assertThat(foreldrepenger.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(foreldrepenger.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepenger.getDifferanse()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(foreldrepenger.getMotregning()).isEqualTo(BigDecimal.valueOf(-517));
        assertThat(foreldrepenger.getResultat()).isEqualTo(BigDecimal.valueOf(14369));
        assertThat(foreldrepenger.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepenger.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(14369));

        // Sjekker resultat for sykepenger
        SimulertBeregning sykepenger = periode.getBeregningPerFagområde().get(SP);
        assertThat(sykepenger.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(1551));
        assertThat(sykepenger.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2068));
        assertThat(sykepenger.getDifferanse()).isEqualTo(BigDecimal.valueOf(-517));
        assertThat(sykepenger.getMotregning()).isEqualTo(BigDecimal.valueOf(517));
        assertThat(sykepenger.getResultat()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepenger.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepenger.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void beregnerPosteringerUtenInntrekk() {
        // Arrange
        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder().medYtelseType(YtelseType.FP)
                .medAktørId("1234")
                .medEksternReferanse(new BehandlingRef(12345L))
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, YTEL, K, 8000))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, YTEL, D, 7000))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, JUST, D, 1000))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FP, YTEL, D, 7000))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FP, JUST, K, 1000))
                                // Uten inntrekk
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, YTEL, K, 8000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, YTEL, D, 7000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, YTEL, D, 1000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FP, FEIL, D, 1000, true))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FP, YTEL, D, 7000, true))
                                .build())
                        .build())
                .build();

        // Act
        SimulertBeregningResultat simulertBeregningResultat = simuleringBeregningTjeneste.hentBeregningsresultatMedOgUtenInntrekk(simuleringGrunnlag);

        // Assert - resultat med inntrekk
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker = simulertBeregningResultat.getBeregningResultat().getBeregningPerMottaker();
        assertThat(beregningPerMottaker.entrySet()).hasSize(1);
        List<SimulertBeregningPeriode> perioder = beregningPerMottaker.values().iterator().next();
        assertThat(perioder).hasSize(2);
        Optional<SimulertBeregningPeriode> september = finnPeriode(perioder, LocalDate.of(2017, 9, 1));
        assertThat(september).isPresent();
        assertThat(september.get().getInntrekkNesteMåned()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(september.get().getResultat()).isEqualTo(BigDecimal.ZERO);


        // Assert - resultat uten inntrekk
        Optional<BeregningResultat> optResultatUtenInntrekk = simulertBeregningResultat.getBeregningResultatUtenInntrekk();
        assertThat(optResultatUtenInntrekk).isPresent();
        BeregningResultat beregningResultat = optResultatUtenInntrekk.get();
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottakerUtenInntrekk = beregningResultat.getBeregningPerMottaker();
        assertThat(beregningPerMottakerUtenInntrekk.entrySet()).hasSize(1);
        List<SimulertBeregningPeriode> perioderUtenInntrekk = beregningPerMottakerUtenInntrekk.values().iterator().next();
        assertThat(perioderUtenInntrekk).hasSize(2);
        Optional<SimulertBeregningPeriode> septemberUtenInntrekk = finnPeriode(perioderUtenInntrekk, LocalDate.of(2017, 9, 1));
        assertThat(septemberUtenInntrekk).isPresent();
        assertThat(septemberUtenInntrekk.get().getInntrekkNesteMåned()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(septemberUtenInntrekk.get().getResultat()).isEqualTo(BigDecimal.valueOf(-1000));
    }

    @Test
    void finnerNesteUtbetalingsperiodeForMottakere() {
        // Arrange
        LocalDate idag = LocalDate.now();

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medEksternReferanse(new BehandlingRef(345L))
                .medYtelseType(YtelseType.FP)
                .medAktørId("12345")
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(
                                SimuleringMottaker.builder()
                                        .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                        .medSimulertPostering(postering("01.09.2018-30.09.2018", FP, YTEL, D, 5029, idag))
                                        .medSimulertPostering(postering("01.10.2018-31.10.2018", FP, YTEL, D, 517, idag))
                                        .medSimulertPostering(postering("01.11.2018-30.11.2018", FP, YTEL, D, 14886, idag.plusWeeks(1))) // Neste utbetalingsperiode
                                        .build())
                        .medSimuleringMottaker(
                                SimuleringMottaker.builder()
                                        .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                        .medSimulertPostering(postering("01.10.2018-31.10.2018", FP, YTEL, D, 5029, idag))
                                        .medSimulertPostering(postering("01.11.2018-30.11.2018", FP, YTEL, D, 517, idag))
                                        .medSimulertPostering(postering("01.12.2018-31.12.2018", FP, YTEL, D, 14886, idag.plusWeeks(1))) // Neste utbetalingsperiode
                                        .build())
                        .build())
                .build();

        // Act
        BeregningResultat resultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);

        // Assert
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker = resultat.getBeregningPerMottaker();
        Optional<Mottaker> mottakerBruker = beregningPerMottaker.keySet().stream().filter(m -> m.mottakerType().equals(MottakerType.BRUKER)).findFirst();
        assertThat(mottakerBruker).isPresent();
        assertThat(mottakerBruker.get().nesteUtbetalingsperiodeFom()).isEqualTo(LocalDate.of(2018, 11, 1));
        assertThat(mottakerBruker.get().nesteUtbetalingsperiodeTom()).isEqualTo(LocalDate.of(2018, 11, 30));

        Optional<Mottaker> mottakerArbg = beregningPerMottaker.keySet().stream().filter(m -> m.mottakerType().equals(MottakerType.ARBG_ORG)).findFirst();
        assertThat(mottakerArbg).isPresent();
        assertThat(mottakerArbg.get().nesteUtbetalingsperiodeFom()).isEqualTo(LocalDate.of(2018, 12, 1));
        assertThat(mottakerArbg.get().nesteUtbetalingsperiodeTom()).isEqualTo(LocalDate.of(2018, 12, 31));
    }

    private Optional<SimulertBeregningPeriode> finnPeriode(List<SimulertBeregningPeriode> perioder, LocalDate fom) {
        return perioder.stream()
                .filter(p -> p.getPeriode().getPeriodeFom().isEqual(fom))
                .findFirst();
    }

    private SimulertPostering postering(String periode, Fagområde fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, false, LocalDate.now());
    }

    private SimulertPostering postering(String periode, Fagområde fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp, LocalDate forfallsdato) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, false, forfallsdato);
    }

    private SimulertPostering postering(String periode, Fagområde fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp, boolean utenInntrekk) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, utenInntrekk, LocalDate.now());
    }


    private SimulertPostering postering(String periode, Fagområde fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp, boolean utenInntrekk,
                                        LocalDate forfallsdato) {
        DateTimeFormatter datoformat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate fom = LocalDate.parse(periode.split("-")[0], datoformat);
        LocalDate tom = LocalDate.parse(periode.split("-")[1], datoformat);
        return SimulertPostering.builder()
                .medPosteringType(posteringType)
                .medBetalingType(betalingType)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medFom(fom)
                .medTom(tom)
                .medFagOmraadeKode(fagOmrådeKode)
                .utenInntrekk(utenInntrekk)
                .medForfallsdato(forfallsdato)
                .build();
    }
}
