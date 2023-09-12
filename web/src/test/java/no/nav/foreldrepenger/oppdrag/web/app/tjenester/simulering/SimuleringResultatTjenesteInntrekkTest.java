package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static java.time.LocalDate.now;
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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.RadId;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

@ExtendWith({JpaExtension.class})
class SimuleringResultatTjenesteInntrekkTest {

    private SimuleringRepository simuleringRepository;

    private PersonTjeneste hentNavnTjeneste = Mockito.mock(PersonTjeneste.class);
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private SimuleringResultatTjeneste simuleringResultatTjeneste;

    private String aktørId = "0";

    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository,
                hentNavnTjeneste, simuleringBeregningTjeneste);
    }

    @Test
    void henterBareDetaljertResultatUtenInntrekkDersomFørsteResultatHarBådeFeilutbetalingOgInntrekkNesteUtbetalingsperiode() {
        var behandlingId = 7654L;

        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var førsteMånedStart = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        var førsteMånedSlutt = førsteMånedStart.plusMonths(1).minusDays(1);

        var nesteMånedStart = nesteForfallsdato.withDayOfMonth(1);
        var nesteMånedSlutt = nesteMånedStart.plusMonths(1).minusDays(1);


        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Posteringer første måned - med inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 14952, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, K, 46992, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, FEIL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, K, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                // Posteringer neste utbetalingsperiode - med inntrekk
                                .medSimulertPostering(postering(FP, JUST, K, 10680, nesteMånedStart, nesteMånedSlutt, nesteForfallsdato))
                                .medSimulertPostering(postering(FP, YTEL, D, 44856, nesteMånedStart, nesteMånedSlutt, nesteForfallsdato))
                                // Posteringer første måned - uten inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 25632, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, YTEL, K, 46992, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, FEIL, D, 21360, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 21360, førsteMånedStart, førsteMånedSlutt, true, now()))
                                // Posteringer neste utbetalingsperiode - uten inntrekk inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 44856, nesteMånedStart, nesteMånedSlutt, true, nesteForfallsdato))

                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        assertThat(simuleringDto.get().slåttAvInntrekk()).isTrue();
        assertThat(simuleringDto.get().simuleringResultatUtenInntrekk()).isNull();
        var simuleringResultatDto = simuleringDto.get().simuleringResultat();

        assertThat(simuleringResultatDto.sumFeilutbetaling()).isEqualTo(-21360);
        assertThat(simuleringResultatDto.sumInntrekk()).isZero();
        assertThat(simuleringResultatDto.sumEtterbetaling()).isZero();

        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(1);
        var mottakerDto = simuleringResultatDto.perioderPerMottaker().get(0);
        assertThat(mottakerDto.resultatPerFagområde()).hasSize(1);

        var foreldrepenger = mottakerDto.resultatPerFagområde().get(0);
        assertThat(foreldrepenger.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FP);
        assertThat(foreldrepenger.rader()).hasSize(3);

        // Sjekker nytt beløp
        var nyttBeløpOptional = finnRadMedRadId(foreldrepenger.rader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpOptional).isPresent();
        var nyttBeløp = nyttBeløpOptional.get();
        assertThat(nyttBeløp.resultaterPerMåned()).hasSize(2);
        // Skal være sortert på dato
        assertThat(nyttBeløp.resultaterPerMåned().get(0).beløp()).isEqualTo(25632);
        assertThat(nyttBeløp.resultaterPerMåned().get(1).beløp()).isEqualTo(44856);

        // Sjekker tidligere utbetalt beløp
        var tidligereUtbOptional = finnRadMedRadId(foreldrepenger.rader(), RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbOptional).isPresent();
        var tidligereUtbetalt = tidligereUtbOptional.get();
        assertThat(tidligereUtbetalt.resultaterPerMåned()).hasSize(2);
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(0).beløp()).isEqualTo(46992);
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(1).beløp()).isZero();

        // Sjekker differanse
        var differanseOptional = finnRadMedRadId(foreldrepenger.rader(), RadId.DIFFERANSE);
        assertThat(differanseOptional).isPresent();
        var differanse = differanseOptional.get();
        assertThat(differanse.resultaterPerMåned()).hasSize(2);
        assertThat(differanse.resultaterPerMåned().get(0).beløp()).isEqualTo(-21360);
        assertThat(differanse.resultaterPerMåned().get(1).beløp()).isEqualTo(44856);

        assertThat(mottakerDto.resultatOgMotregningRader()).hasSize(2);

        // Sjekker inntrekk neste måned
        var inntrekkNesteMånedOptional = finnRadMedRadId(mottakerDto.resultatOgMotregningRader(), RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekkNesteMånedOptional).isPresent();
        var inntrekkNesteMåned = inntrekkNesteMånedOptional.get();
        assertThat(inntrekkNesteMåned.resultaterPerMåned().get(0).beløp()).isZero();
        assertThat(inntrekkNesteMåned.resultaterPerMåned().get(1).beløp()).isZero();

        // Sjekker resultat
        var resultatOptional = finnRadMedRadId(mottakerDto.resultatOgMotregningRader(), RadId.RESULTAT);
        assertThat(resultatOptional).isPresent();
        var resultat = resultatOptional.get();
        assertThat(resultat.resultaterPerMåned().get(0).beløp()).isEqualTo(-21360);
        assertThat(resultat.resultaterPerMåned().get(1).beløp()).isEqualTo(44856);
    }

    @Test
    void henterBareResultatUtenInntrekkDersomFørsteResultatHarBådeFeilutbetalingOgInntrekkNesteUtbetalingsperiode() {
        var behandlingId = 7654L;

        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var førsteMånedStart = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        var førsteMånedSlutt = førsteMånedStart.plusMonths(1).minusDays(1);

        var nesteMånedStart = nesteForfallsdato.withDayOfMonth(1);
        var nesteMånedSlutt = nesteMånedStart.plusMonths(1).minusDays(1);


        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Posteringer første måned - med inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 14952, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, K, 46992, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, FEIL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, JUST, K, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 10680, førsteMånedStart, førsteMånedSlutt, now()))
                                // Posteringer neste utbetalingsperiode - med inntrekk
                                .medSimulertPostering(postering(FP, JUST, K, 10680, nesteMånedStart, nesteMånedSlutt, nesteForfallsdato))
                                .medSimulertPostering(postering(FP, YTEL, D, 44856, nesteMånedStart, nesteMånedSlutt, nesteForfallsdato))
                                // Posteringer første måned - uten inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 25632, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, YTEL, K, 46992, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, FEIL, D, 21360, førsteMånedStart, førsteMånedSlutt, true, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 21360, førsteMånedStart, førsteMånedSlutt, true, now()))
                                // Posteringer neste utbetalingsperiode - uten inntrekk inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, 44856, nesteMånedStart, nesteMånedSlutt, true, nesteForfallsdato))

                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var resultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingId);

        // Assert
        assertThat(resultatDto).isPresent();
        assertThat(resultatDto.get().slåttAvInntrekk()).isTrue();
        assertThat(resultatDto.get().sumInntrekk()).isZero();
        assertThat(resultatDto.get().sumFeilutbetaling()).isEqualTo(-21360);
    }

    @Test
    void henterResultatMedMotregningMellomYtelser() {
        var behandlingId = 8564L;

        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var periodeFom = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        var periodeTom = periodeFom.plusMonths(1).minusDays(1);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Foreldrepenger
                                .medSimulertPostering(postering(FP, SKAT, K, 5029, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(FP, JUST, K, 517, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, 14886, periodeFom, periodeTom, now()))
                                // Sykepenger
                                .medSimulertPostering(postering(SP, YTEL, D, 1551, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(SP, YTEL, K, 2068, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(SP, JUST, D, 517, periodeFom, periodeTom, now()))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        var simuleringResultatDto = simuleringDto.get().simuleringResultat();
        assertThat(simuleringResultatDto.ingenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.sumEtterbetaling()).isEqualTo(14369);
        assertThat(simuleringResultatDto.sumInntrekk()).isEqualTo(-517);
        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(1);

        var mottakerDto = simuleringResultatDto.perioderPerMottaker().get(0);
        assertThat(mottakerDto.mottakerType()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.BRUKER);

        // Sjekker foreldrepenger
        var foreldrepenger = mottakerDto.resultatPerFagområde().get(0);
        assertThat(foreldrepenger.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FP);
        assertThat(foreldrepenger.rader()).hasSize(1);

        // Sjekker nytt beløp foreldrepenger
        var nyttBeløpOptional = finnRadMedRadId(foreldrepenger.rader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpOptional).isPresent();
        var nyttBeløp = nyttBeløpOptional.get();
        assertThat(nyttBeløp.resultaterPerMåned().get(0).beløp()).isEqualTo(14886);

        // Sjekker sykepenger
        var sykepenger = mottakerDto.resultatPerFagområde().get(1);
        assertThat(sykepenger.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.SP);
        assertThat(sykepenger.rader()).hasSize(3);

        // Sjekker nytt beløp sykepenger
        var nyttBeløpSPOptional = finnRadMedRadId(sykepenger.rader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpSPOptional).isPresent();
        assertThat(nyttBeløpSPOptional.get().resultaterPerMåned().get(0).beløp()).isEqualTo(1551);

        // Sjekker tidligere utbetalt beløp sykepenger
        var tidligereUtbOptional = finnRadMedRadId(sykepenger.rader(), RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbOptional).isPresent();
        assertThat(tidligereUtbOptional.get().resultaterPerMåned().get(0).beløp()).isEqualTo(2068);

        // Sjekker differanse
        var differanseOptional = finnRadMedRadId(sykepenger.rader(), RadId.DIFFERANSE);
        assertThat(differanseOptional).isPresent();
        assertThat(differanseOptional.get().resultaterPerMåned().get(0).beløp()).isEqualTo(-517);

        var resultatOgMotregningRader = mottakerDto.resultatOgMotregningRader();
        assertThat(resultatOgMotregningRader).hasSize(3);

        // Sjekker inntrekk neste måned
        var inntrekkNesteMånedOptional = finnRadMedRadId(resultatOgMotregningRader, RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekkNesteMånedOptional).isPresent();
        assertThat(inntrekkNesteMånedOptional.get().resultaterPerMåned().get(0).beløp()).isZero();

        // Sjekker resultat etter motregning
        var resMotregning = finnRadMedRadId(resultatOgMotregningRader, RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resMotregning).isPresent();
        assertThat(resMotregning.get().resultaterPerMåned().get(0).beløp()).isEqualTo(14369);

        // Sjekker resultat
        var resultat = finnRadMedRadId(resultatOgMotregningRader, RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().resultaterPerMåned().get(0).beløp()).isEqualTo(14369);
    }

    @Test
    void henterResultatMedInntrekk() {
        var behandlingId = 97643L;

        var oktober01 = LocalDate.of(2018, 10, 1);
        var oktober31 = LocalDate.of(2018, 10, 31);

        var november01 = LocalDate.of(2018, 11, 1);
        var november30 = LocalDate.of(2018, 11, 30);
        var desember01 = LocalDate.of(2018, 12, 1);

        var november10 = LocalDate.of(2018, 11, 10);


        var riktigBeregnetBeløp = 34500;
        var tidligereUtbetaltBeløp = 47500;
        var feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringKjørtDato(november10.atStartOfDay())
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Med inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, YTEL, K, tidligereUtbetaltBeløp, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, JUST, D, feilutbetalt, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, november01, november30, desember01))
                                .medSimulertPostering(postering(FP, JUST, K, feilutbetalt, november01, november30, desember01))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringResultatDto> simuleringResultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingId);

        assertThat(simuleringResultatDto).isPresent();
        assertThat(simuleringResultatDto.get().sumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.get().sumInntrekk()).isEqualTo(-feilutbetalt);
        assertThat(simuleringResultatDto.get().slåttAvInntrekk()).isFalse();
    }

    @Test
    void henterDetaljertResultatMedOgUtenInntrekk() {
        var behandlingId = 97643L;

        var oktober01 = LocalDate.of(2018, 10, 1);
        var oktober31 = LocalDate.of(2018, 10, 31);

        var november01 = LocalDate.of(2018, 11, 1);
        var november30 = LocalDate.of(2018, 11, 30);
        var desember01 = LocalDate.of(2018, 12, 1);

        var november10 = LocalDate.of(2018, 11, 10);


        var riktigBeregnetBeløp = 34500;
        var tidligereUtbetaltBeløp = 47500;
        var feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringKjørtDato(november10.atStartOfDay())
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Med inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, YTEL, K, tidligereUtbetaltBeløp, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, JUST, D, feilutbetalt, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, november01, november30, desember01))
                                .medSimulertPostering(postering(FP, JUST, K, feilutbetalt, november01, november30, desember01))
                                // Uten inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, oktober01, oktober31, true, november10))
                                .medSimulertPostering(postering(FP, YTEL, K, tidligereUtbetaltBeløp, oktober01, oktober31, true, november10))
                                .medSimulertPostering(postering(FP, YTEL, D, feilutbetalt, oktober01, oktober31, true, november10))
                                .medSimulertPostering(postering(FP, FEIL, D, feilutbetalt, oktober01, oktober31, true, november10))
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, november01, november30, true, desember01))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medMottakerNummer("12345678")
                                .medSimulertPostering(postering(FP, YTEL, D, 1000, oktober01, oktober31, november10))
                                .medSimulertPostering(postering(FP, YTEL, D, 1000, november01, november30, desember01))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        var simuleringDto = optSimuleringDto.get();

        assertThat(simuleringDto.simuleringResultat().ingenPerioderMedAvvik()).isFalse();
        assertThat(simuleringDto.simuleringResultat().sumInntrekk()).isEqualTo(-feilutbetalt);
        assertThat(simuleringDto.simuleringResultat().sumFeilutbetaling()).isZero();
        assertThat(simuleringDto.simuleringResultat().perioderPerMottaker()).hasSize(2);

        var simuleringForMottakerDto = simuleringDto.simuleringResultat().perioderPerMottaker().get(0);
        assertThat(simuleringForMottakerDto.mottakerType()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.BRUKER);
        assertThat(simuleringForMottakerDto.nesteUtbPeriode().fom()).isEqualTo(november01);
        assertThat(simuleringForMottakerDto.nesteUtbPeriode().tom()).isEqualTo(november30);

        assertThat(simuleringDto.simuleringResultatUtenInntrekk().ingenPerioderMedAvvik()).isFalse();
        assertThat(simuleringDto.simuleringResultatUtenInntrekk().sumFeilutbetaling()).isEqualTo(-feilutbetalt);
        assertThat(simuleringDto.simuleringResultatUtenInntrekk().sumInntrekk()).isZero();
        assertThat(simuleringDto.simuleringResultat().perioderPerMottaker()).hasSize(2);
    }

    @Test
    void henterResultatMedOgUtenInntrekkDerResultatUtenInntrekkErTomt() {
        var behandlingId = 976435L;

        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var periodeFom = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        var periodeTom = periodeFom.plusMonths(1).minusDays(1);

        var riktigBeregnetBeløp = 25600;
        var tidligereUtbetaltBeløp = 29200;
        var feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Med inntrekk
                                .medSimulertPostering(postering(FP, YTEL, D, riktigBeregnetBeløp, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(FP, YTEL, D, feilutbetalt, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(FP, YTEL, K, tidligereUtbetaltBeløp, periodeFom, periodeTom, now()))
                                .medSimulertPostering(postering(FP, FEIL, D, feilutbetalt, periodeFom, periodeTom, now()))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        var simuleringDto = optSimuleringDto.get();

        assertThat(simuleringDto.simuleringResultat()).isNotNull();
        assertThat(simuleringDto.simuleringResultatUtenInntrekk()).isNull();
    }

    private Optional<SimuleringDto.SimuleringResultatRadDto> finnRadMedRadId(List<SimuleringDto.SimuleringResultatRadDto> resultatOgMotregningRader, RadId radId) {
        return resultatOgMotregningRader.stream().filter(r -> r.feltnavn().equals(radId)).findFirst();
    }

    private LocalDate finnNesteForfallsdatoBasertPåDagensDato() {
        var dagensDato = LocalDate.now();
        LocalDate datoKjøres;
        if (dagensDato.getDayOfMonth() >= 20) {
            datoKjøres = dagensDato.plusMonths(1).withDayOfMonth(20);
        } else {
            datoKjøres = dagensDato.withDayOfMonth(20);
        }
        return datoKjøres;
    }

    private SimulertPostering postering(Fagområde fagOmrådeKode, PosteringType posteringType, BetalingType betalingType,
                                        int beløp, LocalDate fom, LocalDate tom, LocalDate forfallsdato) {
        return postering(fagOmrådeKode, posteringType, betalingType, beløp, fom, tom, false, forfallsdato);
    }


    private SimulertPostering postering(Fagområde fagOmrådeKode, PosteringType posteringType, BetalingType betalingType,
                                        int beløp, LocalDate fom, LocalDate tom, boolean utenInntrekk, LocalDate forfallsdato) {
        return SimulertPostering.builder()
                .medFagOmraadeKode(fagOmrådeKode)
                .medFom(fom)
                .medTom(tom)
                .medBetalingType(betalingType)
                .medPosteringType(posteringType)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medForfallsdato(forfallsdato)
                .utenInntrekk(utenInntrekk)
                .build();
    }

}
