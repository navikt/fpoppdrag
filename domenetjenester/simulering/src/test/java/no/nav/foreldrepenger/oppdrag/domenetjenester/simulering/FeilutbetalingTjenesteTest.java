package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.PeriodeDto;
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

public class FeilutbetalingTjenesteTest {

    private static final YearMonth JANUAR = YearMonth.of(2019, Month.JANUARY);
    private static final YearMonth FEBRUAR = YearMonth.of(2019, Month.FEBRUARY);

    private static final LocalDate januar_01_2019 = LocalDate.of(2019, 1, 1);
    private static final LocalDate januar_15_2019 = LocalDate.of(2019, 1, 15);
    private static final LocalDate januar_16_2019 = LocalDate.of(2019, 1, 16);
    private static final LocalDate januar_21_2019 = LocalDate.of(2019, 1, 21);
    private static final LocalDate januar_22_2019 = LocalDate.of(2019, 1, 22);
    private static final LocalDate januar_31_2019 = LocalDate.of(2019, 1, 31);

    private static final String kontonr = "45678";

    @Test
    public void finnerFeilutbetaltePerioderFraSammenhengendePeriode() {
        //Arrange
        SimuleringMottaker mottaker = SimuleringMottaker.builder().medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer").build();
        mottaker.leggTilSimulertPostering(lagFeilUtbetalingPostering(januar_15_2019, januar_22_2019, 6000));
        mottaker.leggTilSimulertPostering(lagYtelsePostering(januar_15_2019, januar_22_2019, 6000));

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(1245L))
                .medAktørId("789")
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(mottaker)
                        .build())
                .build();

        // Act
        Optional<FeilutbetaltePerioderDto> perioderDto = FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(simuleringGrunnlag);

        //Assert
        List<PeriodeDto> perioder = perioderDto.get().getPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getFom()).isEqualTo(januar_15_2019);
        assertThat(perioder.get(0).getTom()).isEqualTo(januar_22_2019);
    }

    @Test
    public void finnerFeilutbetaltePerioderFraOppsplittetPeriode() {
        //Arrange
        SimuleringMottaker mottaker = SimuleringMottaker.builder().medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer").build();
        mottaker.leggTilSimulertPostering(lagFeilUtbetalingPostering(januar_01_2019, januar_15_2019, 6000));
        mottaker.leggTilSimulertPostering(lagFeilUtbetalingPostering(januar_16_2019, januar_21_2019, 6000));
        mottaker.leggTilSimulertPostering(lagFeilUtbetalingPostering(januar_22_2019, januar_31_2019, 6000));
        mottaker.leggTilSimulertPostering(lagYtelsePostering(januar_15_2019, januar_22_2019, 6000));

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(1245L))
                .medAktørId("789")
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(mottaker)
                        .build())
                .build();

        // Act
        Optional<FeilutbetaltePerioderDto> perioderDto = FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(simuleringGrunnlag);

        //Assert
        List<PeriodeDto> perioder = perioderDto.get().getPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getFom()).isEqualTo(januar_01_2019);
        assertThat(perioder.get(0).getTom()).isEqualTo(januar_31_2019);
    }

    @Test
    public void finnerMånederMedFeilutbetaling() {

        // Arrange
        List<SimulertPostering> posteringer = new ArrayList<>();
        posteringer.addAll(opprettFeilutbetaling(JANUAR, 3600));

        posteringer.addAll(opprettFeilutbetaling(FEBRUAR, 2300));
        posteringer.add(opprettDebetPostering(LocalDate.of(2019, Month.MARCH, 15), LocalDate.of(2019, Month.MARCH, 31), 16589));

        // Act
        Map<YearMonth, List<SimulertPostering>> resultat = FeilutbetalingTjeneste.finnMånederMedFeilutbetaling(posteringer);

        // Assert
        assertThat(resultat.keySet()).containsExactlyInAnyOrder(JANUAR, FEBRUAR);
    }

    @Test
    public void beregnerDagsats() {
        // Arrange
        int dagsats = 250;
        SimulertPostering postering = opprettKreditPostering(LocalDate.of(2019, 1, 10), LocalDate.of(2019, 1, 31), dagsats * 16);

        // Act
        BigDecimal resultatDagsats = FeilutbetalingTjeneste.beregnDagsats(postering);

        // Assert
        assertThat(resultatDagsats).isEqualTo(BigDecimal.valueOf(dagsats));
    }

    @Test
    public void slårSammenSammenhengendePerioder() {
        // Arrange
        List<Periode> perioder = new ArrayList<>();

        // Første periode: 14.03.2019 - 18.03.2019
        Periode førstePeriode = new Periode(LocalDate.of(2019, 3, 14), LocalDate.of(2019, 3, 18));
        perioder.add(new Periode(LocalDate.of(2019, 3, 14), LocalDate.of(2019, 3, 15)));
        perioder.add(new Periode(LocalDate.of(2019, 3, 18), LocalDate.of(2019, 3, 18)));

        // Andre periode: 20.03.2019 - 30.03.2019
        Periode andrePeriode = new Periode(LocalDate.of(2019, 3, 20), LocalDate.of(2019, 3, 30));
        perioder.add(new Periode(LocalDate.of(2019, 3, 20), LocalDate.of(2019, 3, 23)));
        perioder.add(new Periode(LocalDate.of(2019, 3, 25), LocalDate.of(2019, 3, 30)));

        // Tredje periode 02.04.2019 - 02.04.2019
        Periode tredjePeriode = new Periode(LocalDate.of(2019, 4, 2), LocalDate.of(2019, 4, 2));
        perioder.add(tredjePeriode);

        // Act
        List<Periode> sammenslåttePerioder = FeilutbetalingTjeneste.slåSammenSammenhengendePerioder(perioder);

        // Assert
        assertThat(sammenslåttePerioder).containsExactly(førstePeriode, andrePeriode, tredjePeriode);
    }

    @Test
    public void returnererOptionalEmptyHvisIngenPosteringerForMottakerBruker() {
        // Arrange
        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(1245L))
                .medAktørId("789")
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder().medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .build())
                        .build())
                .build();

        // Act
        Optional<FeilutbetaltePerioderDto> perioderDto = FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(simuleringGrunnlag);

        // Assert
        assertThat(perioderDto).isNotPresent();
    }


    private List<SimulertPostering> opprettFeilutbetaling(YearMonth måned, long beløp) {
        LocalDate fom = måned.atDay(1);
        LocalDate tom = måned.atEndOfMonth();
        List<SimulertPostering> posteringer = new ArrayList<>();
        posteringer.add(lagFeilUtbetalingPostering(fom, tom, beløp));
        posteringer.add(lagYtelsePostering(fom, tom, beløp));
        return posteringer;
    }

    SimulertPostering lagYtelsePostering(LocalDate fom, LocalDate tom, long beløp) {
        return new SimulertPostering.Builder()
                .medFagOmraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medBetalingType(BetalingType.DEBIT)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medPosteringType(PosteringType.YTELSE)
                .medFom(fom)
                .medTom(tom).build();
    }

    SimulertPostering lagFeilUtbetalingPostering(LocalDate fom, LocalDate tom, long beløp) {
        return new SimulertPostering.Builder()
                .medFagOmraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medBetalingType(BetalingType.DEBIT)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medPosteringType(PosteringType.FEILUTBETALING)
                .medFom(fom)
                .medTom(tom).build();
    }

    // Postering for tidligere utbetalt beløp
    private SimulertPostering opprettKreditPostering(LocalDate fom, LocalDate tom, long beløp) {
        return new SimulertPostering.Builder()
                .medFagOmraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medBetalingType(BetalingType.KREDIT)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medPosteringType(PosteringType.YTELSE)
                .medFom(fom)
                .medTom(tom).build();
    }

    // Postering for nytt beregnet beløp
    private SimulertPostering opprettDebetPostering(LocalDate fom, LocalDate tom, long beløp) {
        return new SimulertPostering.Builder()
                .medFagOmraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medBetalingType(BetalingType.DEBIT)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medPosteringType(PosteringType.YTELSE)
                .medFom(fom)
                .medTom(tom)
                .build();
    }
}
