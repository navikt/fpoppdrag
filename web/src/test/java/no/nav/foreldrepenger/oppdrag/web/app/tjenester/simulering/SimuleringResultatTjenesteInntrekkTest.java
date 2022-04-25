package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static java.time.LocalDate.now;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.D;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.K;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.SP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEIL;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.SKAT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.JUST;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
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
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.DetaljertSimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.RadId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringForMottakerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerFagområdeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatRadDto;

@ExtendWith({JpaExtension.class})
public class SimuleringResultatTjenesteInntrekkTest {

    private SimuleringRepository simuleringRepository;

    private HentNavnTjeneste hentNavnTjeneste = Mockito.mock(HentNavnTjeneste.class);
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
    public void henterBareDetaljertResultatUtenInntrekkDersomFørsteResultatHarBådeFeilutbetalingOgInntrekkNesteUtbetalingsperiode() {
        Long behandlingId = 7654L;

        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate førsteMånedStart = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        LocalDate førsteMånedSlutt = førsteMånedStart.plusMonths(1).minusDays(1);

        LocalDate nesteMånedStart = nesteForfallsdato.withDayOfMonth(1);
        LocalDate nesteMånedSlutt = nesteMånedStart.plusMonths(1).minusDays(1);


        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
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
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        assertThat(simuleringDto.get().isSlåttAvInntrekk()).isTrue();
        assertThat(simuleringDto.get().getSimuleringResultatUtenInntrekk()).isNull();
        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();

        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isEqualTo(-21360);
        assertThat(simuleringResultatDto.getSumInntrekk()).isEqualTo(0);
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(0);

        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);
        SimuleringForMottakerDto mottakerDto = simuleringResultatDto.getPerioderPerMottaker().get(0);
        assertThat(mottakerDto.getResultatPerFagområde()).hasSize(1);

        SimuleringResultatPerFagområdeDto foreldrepenger = mottakerDto.getResultatPerFagområde().get(0);
        assertThat(foreldrepenger.getFagOmrådeKode()).isEqualTo(FP);
        assertThat(foreldrepenger.getRader()).hasSize(3);

        // Sjekker nytt beløp
        Optional<SimuleringResultatRadDto> nyttBeløpOptional = finnRadMedRadId(foreldrepenger.getRader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpOptional).isPresent();
        SimuleringResultatRadDto nyttBeløp = nyttBeløpOptional.get();
        assertThat(nyttBeløp.getResultaterPerMåned()).hasSize(2);
        // Skal være sortert på dato
        assertThat(nyttBeløp.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(25632);
        assertThat(nyttBeløp.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(44856);

        // Sjekker tidligere utbetalt beløp
        Optional<SimuleringResultatRadDto> tidligereUtbOptional = finnRadMedRadId(foreldrepenger.getRader(), RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbOptional).isPresent();
        SimuleringResultatRadDto tidligereUtbetalt = tidligereUtbOptional.get();
        assertThat(tidligereUtbetalt.getResultaterPerMåned()).hasSize(2);
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(46992);
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(0);

        // Sjekker differanse
        Optional<SimuleringResultatRadDto> differanseOptional = finnRadMedRadId(foreldrepenger.getRader(), RadId.DIFFERANSE);
        assertThat(differanseOptional).isPresent();
        SimuleringResultatRadDto differanse = differanseOptional.get();
        assertThat(differanse.getResultaterPerMåned()).hasSize(2);
        assertThat(differanse.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(-21360);
        assertThat(differanse.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(44856);

        assertThat(mottakerDto.getResultatOgMotregningRader()).hasSize(2);

        // Sjekker inntrekk neste måned
        Optional<SimuleringResultatRadDto> inntrekkNesteMånedOptional = finnRadMedRadId(mottakerDto.getResultatOgMotregningRader(), RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekkNesteMånedOptional).isPresent();
        SimuleringResultatRadDto inntrekkNesteMåned = inntrekkNesteMånedOptional.get();
        assertThat(inntrekkNesteMåned.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(0);
        assertThat(inntrekkNesteMåned.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(0);

        // Sjekker resultat
        Optional<SimuleringResultatRadDto> resultatOptional = finnRadMedRadId(mottakerDto.getResultatOgMotregningRader(), RadId.RESULTAT);
        assertThat(resultatOptional).isPresent();
        SimuleringResultatRadDto resultat = resultatOptional.get();
        assertThat(resultat.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(-21360);
        assertThat(resultat.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(44856);
    }

    @Test
    public void henterBareResultatUtenInntrekkDersomFørsteResultatHarBådeFeilutbetalingOgInntrekkNesteUtbetalingsperiode() {
        Long behandlingId = 7654L;

        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate førsteMånedStart = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        LocalDate førsteMånedSlutt = førsteMånedStart.plusMonths(1).minusDays(1);

        LocalDate nesteMånedStart = nesteForfallsdato.withDayOfMonth(1);
        LocalDate nesteMånedSlutt = nesteMånedStart.plusMonths(1).minusDays(1);


        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
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
        Optional<SimuleringResultatDto> resultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingId);

        // Assert
        assertThat(resultatDto).isPresent();
        assertThat(resultatDto.get().isSlåttAvInntrekk()).isTrue();
        assertThat(resultatDto.get().getSumInntrekk()).isEqualTo(0);
        assertThat(resultatDto.get().getSumFeilutbetaling()).isEqualTo(-21360);
    }

    @Test
    public void henterResultatMedMotregningMellomYtelser() {
        Long behandlingId = 8564L;

        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate periodeFom = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        LocalDate periodeTom = periodeFom.plusMonths(1).minusDays(1);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
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
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultatDto.isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(14369);
        assertThat(simuleringResultatDto.getSumInntrekk()).isEqualTo(-517);
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);

        SimuleringForMottakerDto mottakerDto = simuleringResultatDto.getPerioderPerMottaker().get(0);
        assertThat(mottakerDto.getMottakerType()).isEqualTo(MottakerType.BRUKER);

        // Sjekker foreldrepenger
        SimuleringResultatPerFagområdeDto foreldrepenger = mottakerDto.getResultatPerFagområde().get(0);
        assertThat(foreldrepenger.getFagOmrådeKode()).isEqualTo(FP);
        assertThat(foreldrepenger.getRader()).hasSize(1);

        // Sjekker nytt beløp foreldrepenger
        Optional<SimuleringResultatRadDto> nyttBeløpOptional = finnRadMedRadId(foreldrepenger.getRader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpOptional).isPresent();
        SimuleringResultatRadDto nyttBeløp = nyttBeløpOptional.get();
        assertThat(nyttBeløp.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(14886);

        // Sjekker sykepenger
        SimuleringResultatPerFagområdeDto sykepenger = mottakerDto.getResultatPerFagområde().get(1);
        assertThat(sykepenger.getFagOmrådeKode()).isEqualTo(SP);
        assertThat(sykepenger.getRader()).hasSize(3);

        // Sjekker nytt beløp sykepenger
        Optional<SimuleringResultatRadDto> nyttBeløpSPOptional = finnRadMedRadId(sykepenger.getRader(), RadId.NYTT_BELØP);
        assertThat(nyttBeløpSPOptional).isPresent();
        assertThat(nyttBeløpSPOptional.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(1551);

        // Sjekker tidligere utbetalt beløp sykepenger
        Optional<SimuleringResultatRadDto> tidligereUtbOptional = finnRadMedRadId(sykepenger.getRader(), RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbOptional).isPresent();
        assertThat(tidligereUtbOptional.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(2068);

        // Sjekker differanse
        Optional<SimuleringResultatRadDto> differanseOptional = finnRadMedRadId(sykepenger.getRader(), RadId.DIFFERANSE);
        assertThat(differanseOptional).isPresent();
        assertThat(differanseOptional.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(-517);

        List<SimuleringResultatRadDto> resultatOgMotregningRader = mottakerDto.getResultatOgMotregningRader();
        assertThat(resultatOgMotregningRader).hasSize(3);

        // Sjekker inntrekk neste måned
        Optional<SimuleringResultatRadDto> inntrekkNesteMånedOptional = finnRadMedRadId(resultatOgMotregningRader, RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekkNesteMånedOptional).isPresent();
        assertThat(inntrekkNesteMånedOptional.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(0);

        // Sjekker resultat etter motregning
        Optional<SimuleringResultatRadDto> resMotregning = finnRadMedRadId(resultatOgMotregningRader, RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resMotregning).isPresent();
        assertThat(resMotregning.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(14369);

        // Sjekker resultat
        Optional<SimuleringResultatRadDto> resultat = finnRadMedRadId(resultatOgMotregningRader, RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getResultaterPerMåned().get(0).getBeløp()).isEqualTo(14369);

    }

    @Test
    public void henterResultatMedInntrekk() {
        Long behandlingId = 97643L;

        LocalDate oktober01 = LocalDate.of(2018, 10, 1);
        LocalDate oktober31 = LocalDate.of(2018, 10, 31);

        LocalDate november01 = LocalDate.of(2018, 11, 1);
        LocalDate november30 = LocalDate.of(2018, 11, 30);
        LocalDate desember01 = LocalDate.of(2018, 12, 1);

        LocalDate november10 = LocalDate.of(2018, 11, 10);


        int riktigBeregnetBeløp = 34500;
        int tidligereUtbetaltBeløp = 47500;
        int feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

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
        assertThat(simuleringResultatDto.get().getSumFeilutbetaling()).isEqualTo(0);
        assertThat(simuleringResultatDto.get().getSumInntrekk()).isEqualTo(-feilutbetalt);
        assertThat(simuleringResultatDto.get().isSlåttAvInntrekk()).isFalse();
    }

    @Test
    public void henterDetaljertResultatMedOgUtenInntrekk() {
        Long behandlingId = 97643L;

        LocalDate oktober01 = LocalDate.of(2018, 10, 1);
        LocalDate oktober31 = LocalDate.of(2018, 10, 31);

        LocalDate november01 = LocalDate.of(2018, 11, 1);
        LocalDate november30 = LocalDate.of(2018, 11, 30);
        LocalDate desember01 = LocalDate.of(2018, 12, 1);

        LocalDate november10 = LocalDate.of(2018, 11, 10);


        int riktigBeregnetBeløp = 34500;
        int tidligereUtbetaltBeløp = 47500;
        int feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

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
        SimuleringDto simuleringDto = optSimuleringDto.get();

        assertThat(simuleringDto.getSimuleringResultat().isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringDto.getSimuleringResultat().getSumInntrekk()).isEqualTo(-feilutbetalt);
        assertThat(simuleringDto.getSimuleringResultat().getSumFeilutbetaling()).isEqualTo(0);
        assertThat(simuleringDto.getSimuleringResultat().getPerioderPerMottaker()).hasSize(2);

        SimuleringForMottakerDto simuleringForMottakerDto = simuleringDto.getSimuleringResultat().getPerioderPerMottaker().get(0);
        assertThat(simuleringForMottakerDto.getMottakerType()).isEqualTo(MottakerType.BRUKER);
        assertThat(simuleringForMottakerDto.getNesteUtbPeriodeFom()).isEqualTo(november01);
        assertThat(simuleringForMottakerDto.getNestUtbPeriodeTom()).isEqualTo(november30);

        assertThat(simuleringDto.getSimuleringResultatUtenInntrekk().isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringDto.getSimuleringResultatUtenInntrekk().getSumFeilutbetaling()).isEqualTo(-feilutbetalt);
        assertThat(simuleringDto.getSimuleringResultatUtenInntrekk().getSumInntrekk()).isEqualTo(0);
        assertThat(simuleringDto.getSimuleringResultat().getPerioderPerMottaker()).hasSize(2);
    }

    @Test
    public void henterResultatMedOgUtenInntrekkDerResultatUtenInntrekkErTomt() {
        Long behandlingId = 976435L;

        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate periodeFom = nesteForfallsdato.minusMonths(1).withDayOfMonth(1);
        LocalDate periodeTom = periodeFom.plusMonths(1).minusDays(1);

        int riktigBeregnetBeløp = 25600;
        int tidligereUtbetaltBeløp = 29200;
        int feilutbetalt = Math.abs(riktigBeregnetBeløp - tidligereUtbetaltBeløp);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
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
        Optional<SimuleringDto> optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        SimuleringDto simuleringDto = optSimuleringDto.get();

        assertThat(simuleringDto.getSimuleringResultat()).isNotNull();
        assertThat(simuleringDto.getSimuleringResultatUtenInntrekk()).isNull();
    }

    private Optional<SimuleringResultatRadDto> finnRadMedRadId(List<SimuleringResultatRadDto> resultatOgMotregningRader, RadId radId) {
        return resultatOgMotregningRader.stream().filter(r -> r.getFeltnavn().equals(radId)).findFirst();
    }

    private LocalDate finnNesteForfallsdatoBasertPåDagensDato() {
        LocalDate dagensDato = LocalDate.now();
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
