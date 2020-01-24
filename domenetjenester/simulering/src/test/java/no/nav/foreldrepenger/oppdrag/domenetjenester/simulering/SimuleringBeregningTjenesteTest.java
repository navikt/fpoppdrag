package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.DEBIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.KREDIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.FORELDREPENGER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.SYKEPENGER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEILUTBETALING;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FORSKUDSSKATT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.JUSTERING;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.UDEFINERT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTELSE;
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

import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

public class SimuleringBeregningTjenesteTest {

    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();

    @Test
    public void skal_ha_at_tidligere_utbetalt_beløp_er_sum_av_kreditposter_for_ytelse() {

        // Act
        BigDecimal tidligereUtbetaltBeløp = SimuleringBeregningTjeneste.beregnTidligereUtbetaltBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, KREDIT, 3000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, KREDIT, 2000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1500),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, KREDIT, 100),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, DEBIT, 200)));

        //Assert
        assertThat(tidligereUtbetaltBeløp).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    public void skal_ha_at_nytt_beløp_er_sum_av_debetposter_for_ytelse() {
        // Act
        BigDecimal nyttBeløp = SimuleringBeregningTjeneste.beregnNyttBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, KREDIT, 3000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, KREDIT, 2000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1500),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, KREDIT, 100),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, DEBIT, 200)));

        // Assert
        assertThat(nyttBeløp).isEqualTo(BigDecimal.valueOf(2500));
    }

    @Test
    public void skal_ha_at_feilutbetalt_beløp_er_sum_av_feilutbetaling_poster() {

        // Act
        BigDecimal feilutbetaltBeløp = SimuleringBeregningTjeneste.beregnFeilutbetaltBeløp(Arrays.asList(
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1500),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, KREDIT, 100),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, FEILUTBETALING, DEBIT, 100),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, FEILUTBETALING, DEBIT, 200)));

        // Assert
        assertThat(feilutbetaltBeløp).isEqualTo(BigDecimal.valueOf(300));
    }

    @Test
    public void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_etterbetaling() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, KREDIT, 2000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1500),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, KREDIT, 100),
                postering("01.09.2018-30.09.2018", FORELDREPENGER, FORSKUDSSKATT, DEBIT, 200)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode periode = simulertBeregningPerioder.get(0);
        assertThat(periode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(periode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2018, 9, 30));

        assertThat(periode.getBeregningPerFagområde()).containsOnlyKeys(FORELDREPENGER);
        SimulertBeregning simulertBeregning = periode.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(simulertBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(simulertBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(simulertBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getResultat()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(simulertBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
        assertThat(simulertBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_feilutbetaling() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 8928),
                postering("06.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 5958),
                postering("06.09.2017-30.09.2017", FORELDREPENGER, YTELSE, KREDIT, 14886),
                postering("06.09.2017-30.09.2017", FORELDREPENGER, FEILUTBETALING, DEBIT, 8928)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode periode = simulertBeregningPerioder.get(0);
        assertThat(periode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2017, 9, 1));
        assertThat(periode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2017, 9, 30));

        assertThat(periode.getBeregningPerFagområde()).containsOnlyKeys(FORELDREPENGER);
        SimulertBeregning simulertBeregning = periode.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(simulertBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(simulertBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(5958));
        assertThat(simulertBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getResultat()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.valueOf(-8928));
        assertThat(simulertBeregning.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
        assertThat(simulertBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_beregne_posteringer_pr_måned_og_fagområde_scenario_med_sykepenger_og_foreldrepenger() {
        // Act
        List<SimulertBeregningPeriode> simulertBeregningPerioder = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("01.09.2018-30.09.2018", SYKEPENGER, YTELSE, DEBIT, 4000),
                postering("01.09.2018-30.09.2018", SYKEPENGER, YTELSE, KREDIT, 3000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, KREDIT, 2000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1500),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000)));

        // Assert
        assertThat(simulertBeregningPerioder).hasSize(1);
        SimulertBeregningPeriode simulertBeregningPeriode = simulertBeregningPerioder.get(0);
        assertThat(simulertBeregningPeriode.getBeregningPerFagområde()).containsOnlyKeys(FORELDREPENGER, SYKEPENGER);
        assertThat(simulertBeregningPeriode.getPeriode().getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(simulertBeregningPeriode.getPeriode().getPeriodeTom()).isEqualTo(LocalDate.of(2018, 9, 30));

        // Assert - foreldrepenger
        SimulertBeregning foreldrepengerBeregning = simulertBeregningPeriode.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(foreldrepengerBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(foreldrepengerBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(foreldrepengerBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(foreldrepengerBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(foreldrepengerBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepengerBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);

        // Assert - sykepenger
        SimulertBeregning sykepengerBeregning = simulertBeregningPeriode.getBeregningPerFagområde().get(SYKEPENGER);
        assertThat(sykepengerBeregning.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(sykepengerBeregning.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(sykepengerBeregning.getDifferanse()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(sykepengerBeregning.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(sykepengerBeregning.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepengerBeregning.getMotregning()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_summere_justeringskontoer() {
        BigDecimal resultat = SimuleringBeregningTjeneste.beregnMotregning(Arrays.asList(
                postering("16.09.2018-30.09.2018", FORELDREPENGER, JUSTERING, DEBIT, 1000),
                postering("16.09.2018-30.09.2018", FORELDREPENGER, JUSTERING, KREDIT, 500),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, KREDIT, 3000),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000)));

        assertThat(resultat).isEqualTo(BigDecimal.valueOf(500));

        // Tom liste skal gi sum 0
        assertThat(SimuleringBeregningTjeneste.beregnMotregning(Collections.emptyList())).isEqualTo(BigDecimal.ZERO);

        // Ingen justeringsposter skal gi sum 0
        assertThat(SimuleringBeregningTjeneste.beregnMotregning(Arrays.asList(postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, KREDIT, 3000),
                postering("01.09.2018-15.09.2018", FORELDREPENGER, YTELSE, DEBIT, 1000)))).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void oppretterOppsummeringForForeldrepenger() {
        // Arrange
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();
        BigDecimal feilutbetaltBeløp = BigDecimal.valueOf(-5000);
        BigDecimal inntrekkNesteMåned = BigDecimal.valueOf(-3000);
        BigDecimal etterbetaling = BigDecimal.valueOf(4000);
        Mottaker mottaker = new Mottaker(MottakerType.BRUKER, "12345");
        mottaker.setNesteUtbetalingsperiode(new Periode(LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30)));

        SimulertBeregningPeriode september = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 9, 1), LocalDate.of(2018, 9, 30)))
                .medBeregning(FagOmrådeKode.FORELDREPENGER, SimulertBeregning.builder()
                        .medEtterbetaling(etterbetaling)
                        .build())
                .build();

        SimulertBeregningPeriode oktober = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 31)))
                .medBeregning(FagOmrådeKode.FORELDREPENGER, SimulertBeregning.builder()
                        .medFeilutbetaltBeløp(feilutbetaltBeløp)
                        .build())
                .build();

        SimulertBeregningPeriode november = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30)))
                .medBeregning(FagOmrådeKode.FORELDREPENGER, SimulertBeregning.builder()
                        .medMotregning(inntrekkNesteMåned)
                        .build())
                .build();

        beregningsresultat.put(mottaker, Arrays.asList(september, oktober, november));

        // Act
        Oppsummering oppsummering = simuleringBeregningTjeneste.opprettOppsummering(beregningsresultat, YtelseType.FORELDREPENGER);

        // Assert
        assertThat(oppsummering.getEtterbetaling()).isEqualTo(etterbetaling);
        assertThat(oppsummering.getFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
        assertThat(oppsummering.getInntrekkNesteUtbetaling()).isEqualTo(inntrekkNesteMåned);
        assertThat(oppsummering.getPeriodeFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(oppsummering.getPeriodeTom()).isEqualTo(LocalDate.of(2018, 10, 31));
    }

    @Test
    public void oppretterOppsummeringForEngangsstønad() {
        // Arrange
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();
        BigDecimal feilutbetaltBeløp = BigDecimal.valueOf(-40000);
        Mottaker mottaker = new Mottaker(MottakerType.BRUKER, "12345");
        mottaker.setNesteUtbetalingsperiode(new Periode(LocalDate.of(2018, 11, 1), LocalDate.of(2018, 11, 30)));

        SimulertBeregningPeriode simulertBeregningPeriode = SimulertBeregningPeriode.builder()
                .medPeriode(new Periode(LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 31)))
                .medBeregning(FagOmrådeKode.ENGANGSSTØNAD, SimulertBeregning.builder()
                        .medFeilutbetaltBeløp(feilutbetaltBeløp)
                        .build())
                .build();
        beregningsresultat.put(mottaker, Collections.singletonList(simulertBeregningPeriode));

        // Act
        Oppsummering oppsummering = simuleringBeregningTjeneste.opprettOppsummering(beregningsresultat, YtelseType.ENGANGSTØNAD);

        // Assert
        assertThat(oppsummering.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
        assertThat(oppsummering.getFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
        assertThat(oppsummering.getInntrekkNesteUtbetaling()).isNull();
        assertThat(oppsummering.getPeriodeFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(oppsummering.getPeriodeTom()).isEqualTo(LocalDate.of(2018, 10, 31));
    }

    @Test
    public void skal_beregne_inntrekk_og_feilutbetaling_scenario_over_to_måneder() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for juni, feilutbetaling og inntrekk fra neste måned
                postering("01.06.2017-19.06.2017", FORELDREPENGER, YTELSE, DEBIT, 14952),
                postering("01.06.2017-30.06.2017", FORELDREPENGER, JUSTERING, DEBIT, 10680),
                postering("01.06.2017-30.06.2017", FORELDREPENGER, JUSTERING, DEBIT, 10680),
                postering("01.06.2017-30.06.2017", FORELDREPENGER, YTELSE, KREDIT, 46992),
                postering("24.06.2017-30.06.2017", FORELDREPENGER, FEILUTBETALING, DEBIT, 10680),
                postering("24.06.2017-30.06.2017", FORELDREPENGER, YTELSE, DEBIT, 10680),
                postering("01.06.2017-30.06.2017", FORELDREPENGER, JUSTERING, KREDIT, 10680),
                postering("19.06.2017-23.06.2017", FORELDREPENGER, YTELSE, DEBIT, 10680),

                //Posteringer for juli, med inntrekk
                postering("01.07.2017-31.07.2017", FORELDREPENGER, JUSTERING, KREDIT, 10680),
                postering("03.07.2017-31.07.2017", FORELDREPENGER, YTELSE, DEBIT, 44856)
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
        SimulertBeregning foreldrepengerJuni = resultatJuni.getBeregningPerFagområde().get(FORELDREPENGER);
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
        SimulertBeregning foreldrepengerJuli = resultatJuli.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(foreldrepengerJuli.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepengerJuli.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(44856));
        assertThat(foreldrepengerJuli.getDifferanse()).isEqualTo(BigDecimal.valueOf(44856));
        assertThat(foreldrepengerJuli.getResultat()).isEqualTo(BigDecimal.valueOf(34176));
        assertThat(foreldrepengerJuli.getMotregning()).isEqualTo(BigDecimal.valueOf(-10680));
        assertThat(foreldrepengerJuli.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_beregne_motregning_mellom_to_ytelser_innenfor_samme_måned() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for foreldrepenger
                postering("01.09.2017-30.09.2017", FORELDREPENGER, FORSKUDSSKATT, KREDIT, 5029),
                postering("01.09.2017-30.09.2017", FORELDREPENGER, JUSTERING, KREDIT, 517),
                postering("06.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 14886),

                // Posteringer for sykepenger
                postering("01.09.2017-05.09.2017", SYKEPENGER, YTELSE, DEBIT, 1551),
                postering("01.09.2017-06.09.2017", SYKEPENGER, YTELSE, KREDIT, 2068),
                postering("01.09.2017-30.09.2017", SYKEPENGER, JUSTERING, DEBIT, 517)
        ));

        // Assert
        assertThat(resultat).hasSize(1);

        SimulertBeregningPeriode periode = resultat.get(0);
        assertThat(periode.getResultatEtterMotregning()).isEqualTo(BigDecimal.valueOf(14369));
        assertThat(periode.getInntrekkNesteMåned()).isEqualTo(BigDecimal.ZERO);
        assertThat(periode.getResultat()).isEqualTo(BigDecimal.valueOf(14369));

        assertThat(periode.getBeregningPerFagområde().keySet()).hasSize(2);

        // Sjekker resultat for foreldrepenger
        SimulertBeregning foreldrepenger = periode.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(foreldrepenger.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(foreldrepenger.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepenger.getDifferanse()).isEqualTo(BigDecimal.valueOf(14886));
        assertThat(foreldrepenger.getMotregning()).isEqualTo(BigDecimal.valueOf(-517));
        assertThat(foreldrepenger.getResultat()).isEqualTo(BigDecimal.valueOf(14369));
        assertThat(foreldrepenger.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(foreldrepenger.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(14369));

        // Sjekker resultat for sykepenger
        SimulertBeregning sykepenger = periode.getBeregningPerFagområde().get(SYKEPENGER);
        assertThat(sykepenger.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(1551));
        assertThat(sykepenger.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(2068));
        assertThat(sykepenger.getDifferanse()).isEqualTo(BigDecimal.valueOf(-517));
        assertThat(sykepenger.getMotregning()).isEqualTo(BigDecimal.valueOf(517));
        assertThat(sykepenger.getResultat()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepenger.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(sykepenger.getEtterbetaling()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void beregnerPosteringerUtenInntrekk() {
        // Arrange
        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder().medYtelseType(YtelseType.FORELDREPENGER)
                .medAktørId("1234")
                .medEksternReferanse(new BehandlingRef(12345L))
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER)
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, KREDIT, 8000))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 7000))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, JUSTERING, DEBIT, 1000))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FORELDREPENGER, YTELSE, DEBIT, 7000))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FORELDREPENGER, JUSTERING, KREDIT, 1000))
                                // Uten inntrekk
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, KREDIT, 8000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 7000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, YTELSE, DEBIT, 1000, true))
                                .medSimulertPostering(postering("01.09.2017-30.09.2017", FORELDREPENGER, FEILUTBETALING, DEBIT, 1000, true))
                                .medSimulertPostering(postering("01.10.2017-30.10.2017", FORELDREPENGER, YTELSE, DEBIT, 7000, true))
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
    public void finnerNesteUtbetalingsperiodeForMottakere() {
        // Arrange
        LocalDate idag = LocalDate.now();

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medEksternReferanse(new BehandlingRef(345L))
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medAktørId("12345")
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(
                                SimuleringMottaker.builder()
                                        .medMottakerType(MottakerType.BRUKER)
                                        .medSimulertPostering(postering("01.09.2018-30.09.2018", FORELDREPENGER, YTELSE, DEBIT, 5029, idag))
                                        .medSimulertPostering(postering("01.10.2018-31.10.2018", FORELDREPENGER, YTELSE, DEBIT, 517, idag))
                                        .medSimulertPostering(postering("01.11.2018-30.11.2018", FORELDREPENGER, YTELSE, DEBIT, 14886, idag.plusWeeks(1))) // Neste utbetalingsperiode
                                        .build())
                        .medSimuleringMottaker(
                                SimuleringMottaker.builder()
                                        .medMottakerType(MottakerType.ARBG_ORG)
                                        .medSimulertPostering(postering("01.10.2018-31.10.2018", FORELDREPENGER, YTELSE, DEBIT, 5029, idag))
                                        .medSimulertPostering(postering("01.11.2018-30.11.2018", FORELDREPENGER, YTELSE, DEBIT, 517, idag))
                                        .medSimulertPostering(postering("01.12.2018-31.12.2018", FORELDREPENGER, YTELSE, DEBIT, 14886, idag.plusWeeks(1))) // Neste utbetalingsperiode
                                        .build())
                        .build())
                .build();

        // Act
        BeregningResultat resultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);

        // Assert
        Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker = resultat.getBeregningPerMottaker();
        Optional<Mottaker> mottakerBruker = beregningPerMottaker.keySet().stream().filter(m -> m.getMottakerType().equals(MottakerType.BRUKER)).findFirst();
        assertThat(mottakerBruker).isPresent();
        assertThat(mottakerBruker.get().getNesteUtbetalingsperiodeFom()).isEqualTo(LocalDate.of(2018, 11, 1));
        assertThat(mottakerBruker.get().getNesteUtbetalingsperiodeTom()).isEqualTo(LocalDate.of(2018, 11, 30));

        Optional<Mottaker> mottakerArbg = beregningPerMottaker.keySet().stream().filter(m -> m.getMottakerType().equals(MottakerType.ARBG_ORG)).findFirst();
        assertThat(mottakerArbg).isPresent();
        assertThat(mottakerArbg.get().getNesteUtbetalingsperiodeFom()).isEqualTo(LocalDate.of(2018, 12, 1));
        assertThat(mottakerArbg.get().getNesteUtbetalingsperiodeTom()).isEqualTo(LocalDate.of(2018, 12, 31));
    }

    @Test
    public void skal_ta_hensyn_til_eksisterende_kravgrunnlag_når_sum_av_FEIL_posteringer_er_negativ_skal_nytt_beløp_reduseres() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for foreldrepenger
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, KREDIT, 9300),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, KREDIT, 13960),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, DEBIT, 23260),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, FEILUTBETALING, KREDIT, 9300)
        ));

        // Assert
        assertThat(resultat).hasSize(1);
        SimulertBeregningPeriode periode = resultat.get(0);
        assertThat(periode.getResultatEtterMotregning()).isEqualTo(BigDecimal.valueOf(9300));
        assertThat(periode.getInntrekkNesteMåned()).isEqualTo(BigDecimal.ZERO);
        assertThat(periode.getResultat()).isEqualTo(BigDecimal.valueOf(9300));

        assertThat(periode.getBeregningPerFagområde().keySet()).hasSize(1);

        SimulertBeregning foreldrepenger = periode.getBeregningPerFagområde().get(FORELDREPENGER);
        assertThat(foreldrepenger.getNyttBeregnetBeløp()).isEqualTo(BigDecimal.valueOf(23260));
        assertThat(foreldrepenger.getTidligereUtbetaltBeløp()).isEqualTo(BigDecimal.valueOf(13960));

        assertThat(foreldrepenger.getDifferanse()).isEqualTo(BigDecimal.valueOf(9300));
        assertThat(foreldrepenger.getMotregning()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(foreldrepenger.getResultat()).isEqualTo(BigDecimal.valueOf(9300));
        assertThat(foreldrepenger.getFeilutbetaltBeløp()).isEqualTo(BigDecimal.valueOf(9300));
        assertThat(foreldrepenger.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    public void skal_ha_at_etterbetaling_er_0_når_tilbakeførte_trekk_dekker_opp_feilutbetaling() {
        // Act
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, KREDIT, 10000),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, DEBIT, 5000),

                //TREKK-posteringen blir ignorert, men tar med i enhetstesten for å understreke poenget
                postering("01.06.2019-30.06.2019", FORELDREPENGER, UDEFINERT, DEBIT, 5000)
        ));

        assertThat(resultat).hasSize(1);
        SimulertBeregningPeriode periode = resultat.get(0);
        assertThat(periode.getBeregningPerFagområde().keySet()).hasSize(1);
        SimulertBeregning foreldrepenger = periode.getBeregningPerFagområde().get(FORELDREPENGER);

        // Assert
        assertThat(foreldrepenger.getEtterbetaling()).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    public void skal_ha_at_sum_feilutbetaling_er_0_når_det_summert_er_reduksjon_i_feilutbetaling() {
        List<SimulertBeregningPeriode> resultat = simuleringBeregningTjeneste.beregnPosteringerPerMånedOgFagområde(Arrays.asList(
                // Posteringer for foreldrepenger
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, KREDIT, 9300),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, KREDIT, 13960),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, YTELSE, DEBIT, 23260),
                postering("01.06.2019-30.06.2019", FORELDREPENGER, FEILUTBETALING, KREDIT, 9300)
        ));

        Mottaker mottaker = new Mottaker(MottakerType.BRUKER, "1");
        mottaker.setNesteUtbetalingsperiode(new Periode(LocalDate.of(2019, 12, 1), LocalDate.of(2019, 12, 31)));
        Map<Mottaker, List<SimulertBeregningPeriode>> resultatForBruker = Map.of(mottaker, resultat);

        //act
        Oppsummering oppsummering = simuleringBeregningTjeneste.opprettOppsummering(resultatForBruker, YtelseType.FORELDREPENGER);

        //assert
        assertThat(oppsummering.getFeilutbetaling()).isZero();
    }

    private Optional<SimulertBeregningPeriode> finnPeriode(List<SimulertBeregningPeriode> perioder, LocalDate fom) {
        return perioder.stream()
                .filter(p -> p.getPeriode().getPeriodeFom().isEqual(fom))
                .findFirst();
    }

    private SimulertPostering postering(String periode, FagOmrådeKode fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, false, LocalDate.now());
    }

    private SimulertPostering postering(String periode, FagOmrådeKode fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp, LocalDate forfallsdato) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, false, forfallsdato);
    }

    private SimulertPostering postering(String periode, FagOmrådeKode fagOmrådeKode, PosteringType posteringType,
                                        BetalingType betalingType, int beløp, boolean utenInntrekk) {
        return postering(periode, fagOmrådeKode, posteringType, betalingType, beløp, utenInntrekk, LocalDate.now());
    }

    private SimulertPostering postering(String periode, FagOmrådeKode fagOmrådeKode, PosteringType posteringType,
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
