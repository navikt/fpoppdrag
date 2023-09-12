package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.D;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.K;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FPREF;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.REFUTG;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEIL;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.SKAT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.RadId;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringDto;
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
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

@ExtendWith(JpaExtension.class)
class SimuleringResultatTjenesteTest {

    private SimuleringRepository simuleringRepository;

    private final PersonTjeneste hentNavnTjeneste = Mockito.mock(PersonTjeneste.class);
    private final SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private final String aktørId = "0123456789";

    private SimuleringResultatTjeneste simuleringResultatTjeneste;


    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository, hentNavnTjeneste,
                simuleringBeregningTjeneste);
    }

    @Test
    void henter_simulering_resultat_etterbetaling_en_mottaker_ett_fagområde() {
        // Arrange
        var behandlingId = 123L;
        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        var andreMåned = startDato.plusMonths(1);
        var nesteMåned = nesteForfallsdato.withDayOfMonth(1);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // 2 måneder siden, kr 2000 i etterbetaling
                                .medSimulertPostering(
                                        postering(startDato, startDato.plusDays(15), K, YTEL, FP,
                                                3000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(16), andreMåned.minusDays(1), K, YTEL,
                                                FP, 2000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), D, YTEL, FP,
                                                7000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), D, SKAT,
                                                FP, 100))
                                // Forrige måned, kr 4000 i etterbetaling
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), K, YTEL, FP,
                                                6000))
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), D, YTEL, FP,
                                                10000))
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), D, SKAT,
                                                FP, 400))
                                // Neste måned, kr 10000 til utbetaling
                                .medSimulertPostering(
                                        postering(nesteMåned, nesteMåned.plusMonths(1).minusDays(1), D, YTEL,
                                                FP, 10000, nesteForfallsdato))
                                .medSimulertPostering(
                                        postering(nesteMåned, nesteMåned.plusMonths(1).minusDays(1), D,
                                                SKAT, FP, 400, nesteForfallsdato))
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
        assertThat(simuleringResultatDto.periode().fom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.periode().tom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(simuleringResultatDto.sumEtterbetaling()).isEqualTo(6000);
        assertThat(simuleringResultatDto.sumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.sumInntrekk()).isZero();
        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(1);

        var mottakerDto = simuleringResultatDto.perioderPerMottaker().get(0);
        assertThat(mottakerDto.mottakerType()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.BRUKER);

        assertThat(mottakerDto.resultatOgMotregningRader()).hasSize(2);

        // Inntrekk - skal være sortert i riktig rekkefølge
        var inntrekk = mottakerDto.resultatOgMotregningRader().get(0);
        assertThat(inntrekk.feltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekk.resultaterPerMåned()).hasSize(3);
        assertThat(inntrekk.resultaterPerMåned().get(0).beløp()).isZero();
        assertThat(inntrekk.resultaterPerMåned().get(1).beløp()).isZero();
        assertThat(inntrekk.resultaterPerMåned().get(2).beløp()).isZero();

        // Resultat
        var resultat = mottakerDto.resultatOgMotregningRader().get(1);
        assertThat(resultat.feltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(resultat.resultaterPerMåned()).hasSize(3);
        assertThat(resultat.resultaterPerMåned().get(0).beløp()).isEqualTo(2000);
        assertThat(resultat.resultaterPerMåned().get(1).beløp()).isEqualTo(4000);
        assertThat(resultat.resultaterPerMåned().get(2).beløp()).isEqualTo(10000);


        assertThat(mottakerDto.resultatPerFagområde()).hasSize(1);
        assertThat(mottakerDto.resultatPerFagområde().get(0).fagOmrådeKode()).isEqualTo(
            no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FP);
        assertThat(mottakerDto.resultatPerFagområde().get(0).rader()).hasSize(3);

        // Nytt beløp - skal være sortert i riktig rekkefølge
        var nyttBeløp = mottakerDto.resultatPerFagområde().get(0).rader().get(0);
        assertThat(nyttBeløp.feltnavn()).isEqualTo(RadId.NYTT_BELØP);
        assertThat(nyttBeløp.resultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(nyttBeløp.resultaterPerMåned().get(0).periode().fom()).isEqualTo(startDato);
        assertThat(nyttBeløp.resultaterPerMåned().get(0).periode().tom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(nyttBeløp.resultaterPerMåned().get(0).beløp()).isEqualTo(7000);
        // Assert andre periode
        assertThat(nyttBeløp.resultaterPerMåned().get(1).periode().fom()).isEqualTo(andreMåned);
        assertThat(nyttBeløp.resultaterPerMåned().get(1).periode().tom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(nyttBeløp.resultaterPerMåned().get(1).beløp()).isEqualTo(10000);
        // Assert tredje periode
        assertThat(nyttBeløp.resultaterPerMåned().get(2).periode().fom()).isEqualTo(nesteMåned);
        assertThat(nyttBeløp.resultaterPerMåned().get(2).periode().tom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(nyttBeløp.resultaterPerMåned().get(2).beløp()).isEqualTo(10000);

        // Tidligere utbetalt
        var tidligereUtbetalt = mottakerDto.resultatPerFagområde().get(0).rader().get(1);
        assertThat(tidligereUtbetalt.feltnavn()).isEqualTo(RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbetalt.resultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(0).periode().fom()).isEqualTo(startDato);
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(0).periode().tom()).isEqualTo(
                andreMåned.minusDays(1));
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(0).beløp()).isEqualTo(5000);
        // Assert andre periode
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(1).periode().fom()).isEqualTo(andreMåned);
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(1).periode().tom()).isEqualTo(
                nesteMåned.minusDays(1));
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(1).beløp()).isEqualTo(6000);
        // Assert tredje periode
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(2).periode().fom()).isEqualTo(nesteMåned);
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(2).periode().tom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(tidligereUtbetalt.resultaterPerMåned().get(2).beløp()).isZero();


        // Differanse
        var differanse = mottakerDto.resultatPerFagområde().get(0).rader().get(2);
        assertThat(differanse.feltnavn()).isEqualTo(RadId.DIFFERANSE);
        assertThat(differanse.resultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(differanse.resultaterPerMåned().get(0).periode().fom()).isEqualTo(startDato);
        assertThat(differanse.resultaterPerMåned().get(0).periode().tom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(differanse.resultaterPerMåned().get(0).beløp()).isEqualTo(2000);
        // Assert andre periode
        assertThat(differanse.resultaterPerMåned().get(1).periode().fom()).isEqualTo(andreMåned);
        assertThat(differanse.resultaterPerMåned().get(1).periode().tom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(differanse.resultaterPerMåned().get(1).beløp()).isEqualTo(4000);
        // Assert tredje periode
        assertThat(differanse.resultaterPerMåned().get(2).periode().fom()).isEqualTo(nesteMåned);
        assertThat(differanse.resultaterPerMåned().get(2).periode().tom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(differanse.resultaterPerMåned().get(2).beløp()).isEqualTo(10000);
    }

    @Test
    void henterSimuleringResultatFlereMottakere() {
        // Arrange
        var behandlingId = 123L;
        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        var andreMåned = startDato.plusMonths(1);

        var fnrArbgiv = "12345678999";
        var aktørIdArbgiver = new AktørId("1111111111111");

        var orgnr = "999999999";
        when(hentNavnTjeneste.hentAktørForFnr(fnrArbgiv)).thenReturn(Optional.of(aktørIdArbgiver));

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // 2 måneder siden, kr 2000 i etterbetaling
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), K, YTEL, FP,
                                                5000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), D, YTEL, FP,
                                                7000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_PRIV).medMottakerNummer("nummer")
                                .medMottakerNummer(fnrArbgiv)
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), K, YTEL,
                                        FPREF, 1000))
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), D, YTEL,
                                        FPREF, 2000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medMottakerNummer(orgnr)
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), D, YTEL,
                                        FPREF, 1000))
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
        assertThat(simuleringResultatDto.periode().fom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.periode().tom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(simuleringResultatDto.sumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.sumInntrekk()).isZero();
        assertThat(simuleringResultatDto.sumEtterbetaling()).isEqualTo(2000);

        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(3);

        // Mottaker = bruker
        var brukerOptional = simuleringResultatDto.perioderPerMottaker()
                .stream()
                .filter(p -> p.mottakerType().equals(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.BRUKER))
                .findFirst();
        assertThat(brukerOptional).isPresent();
        var bruker = brukerOptional.get();
        assertThat(bruker.resultatPerFagområde()).hasSize(1);
        var perFagområdeDto = bruker.resultatPerFagområde().get(0);
        assertThat(perFagområdeDto.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FP);
        assertThat(perFagområdeDto.rader()).hasSize(3);

        assertThat(bruker.resultatOgMotregningRader()).hasSize(2);
        assertThat(bruker.resultatOgMotregningRader().get(0).feltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(bruker.resultatOgMotregningRader().get(0).resultaterPerMåned()).hasSize(1);
        assertThat(bruker.resultatOgMotregningRader().get(0).resultaterPerMåned().get(0).beløp()).isZero();
        assertThat(bruker.resultatOgMotregningRader()
                .get(0)
                .resultaterPerMåned()
                .get(0)
                .periode()
                .fom()).isEqualTo(startDato);
        assertThat(bruker.resultatOgMotregningRader()
                .get(0)
                .resultaterPerMåned()
                .get(0)
                .periode()
                .tom()).isEqualTo(andreMåned.minusDays(1));

        assertThat(bruker.resultatOgMotregningRader().get(1).feltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(bruker.resultatOgMotregningRader().get(1).resultaterPerMåned()).hasSize(1);
        assertThat(bruker.resultatOgMotregningRader().get(1).resultaterPerMåned().get(0).beløp()).isEqualTo(
                2000);
        assertThat(bruker.resultatOgMotregningRader()
                .get(1)
                .resultaterPerMåned()
                .get(0)
                .periode()
                .fom()).isEqualTo(startDato);
        assertThat(bruker.resultatOgMotregningRader()
                .get(1)
                .resultaterPerMåned()
                .get(0)
                .periode()
                .tom()).isEqualTo(andreMåned.minusDays(1));


        // Mottaker = arbeidsgiver med fnr
        var  arbgivPrivOptional = simuleringResultatDto.perioderPerMottaker()
                .stream()
                .filter(p -> p.mottakerType().equals(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.ARBG_PRIV))
                .findFirst();
        assertThat(arbgivPrivOptional).isPresent();
        var arbgivPriv = arbgivPrivOptional.get();
        assertThat(arbgivPriv.mottakerNummer()).isEqualTo(fnrArbgiv);
        assertThat(arbgivPriv.mottakerIdentifikator()).isEqualTo(aktørIdArbgiver.getId());
        assertThat(arbgivPriv.resultatPerFagområde()).hasSize(1);
        var perFagområdeDto1 = arbgivPriv.resultatPerFagområde().get(0);

        assertThat(perFagområdeDto1.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FPREF);
        assertThat(perFagområdeDto1.rader()).hasSize(3);
        assertThat(arbgivPriv.resultatOgMotregningRader()).isEmpty();


        // Mottaker = arbeidsgiver med orgnr
        var arbgivOrgnrOptional = simuleringResultatDto.perioderPerMottaker()
                .stream()
                .filter(p -> p.mottakerType().equals(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.ARBG_ORG))
                .findFirst();
        assertThat(arbgivOrgnrOptional).isPresent();
        var arbgivOrgnr = arbgivOrgnrOptional.get();
        assertThat(arbgivOrgnr.mottakerNummer()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.mottakerIdentifikator()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.resultatPerFagområde()).hasSize(1);
        var perFagområdeDto2 = arbgivOrgnr.resultatPerFagområde().get(0);

        assertThat(perFagområdeDto2.fagOmrådeKode()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.Fagområde.FPREF);
        assertThat(perFagområdeDto2.rader()).hasSize(1);
        assertThat(arbgivOrgnr.resultatOgMotregningRader()).isEmpty();
    }

    @Test
    void finnerSumKunForFagområdeForeldrepengerOgHvisMottakerErBruker() {
        // Arrange
        var behandlingId = 123L;
        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        var andreMåned = startDato.plusMonths(1);

        var fnrArbgiv = "12345678910";
        var navn = "Onkel Skrue";
        var aktørIdArbgiver = new AktørId("1111111111111");
        var orgnr = "999999999";

        when(hentNavnTjeneste.hentAktørForFnr(fnrArbgiv)).thenReturn(Optional.of(aktørIdArbgiver));

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), K, YTEL, FP,
                                                5000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), D, YTEL, FP,
                                                7000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_PRIV).medMottakerNummer("nummer")
                                .medMottakerNummer(fnrArbgiv)
                                .medSimulertPostering(
                                        postering(startDato.minusMonths(1), startDato.minusDays(1), K, YTEL,
                                                FPREF, 1000))
                                .medSimulertPostering(
                                        postering(startDato.minusMonths(1), startDato.minusDays(1), D, YTEL,
                                                FPREF, 2000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medMottakerNummer(orgnr)
                                .medSimulertPostering(
                                        postering(andreMåned, andreMåned.plusMonths(1).minusDays(1), D, YTEL,
                                                FPREF, 1000))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        assertThat(simuleringDto).isPresent();
        var simuleringResultatDto = simuleringDto.get().simuleringResultat();
        assertThat(simuleringResultatDto.sumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(3);
    }

    @Test
    void skalIkkeHaInntrekkOgNesteUtbetalingsperiodeVedEngangsstønad() {
        // Arrange
        var behandlingId = 123456L;
        var datoKjøres = finnNesteForfallsdatoBasertPåDagensDato();
        var startDato = datoKjøres.minusMonths(1).withDayOfMonth(1);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.ES)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), K, YTEL,
                                                REFUTG, 40000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), D, FEIL,
                                                REFUTG, 40000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), D, YTEL,
                                                REFUTG, 40000))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).map(SimuleringDto::simuleringResultat)
            .hasValueSatisfying(simuleringResultatDto -> {
                assertThat(simuleringResultatDto.sumInntrekk()).isNull();
                assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(1);
                assertThat(simuleringResultatDto.perioderPerMottaker().get(0).nesteUtbPeriode().fom()).isNull();
            });
    }

    @Test
    void skalTrunkereDesimalerPåBeløp() {
        // Arrange
        var behandlingId = 123456L;
        var førsteAugust2018 = LocalDate.of(2018, 8, 01);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(førsteAugust2018, førsteAugust2018.plusDays(15), D, YTEL,
                                                FP, 15600.75))
                                .medSimulertPostering(
                                        postering(førsteAugust2018.plusDays(16), førsteAugust2018.plusDays(25), D,
                                                YTEL, FP, 7400.85))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        assertThat(simuleringDto.get().simuleringResultat().sumEtterbetaling()).isEqualTo(23001);
    }

    @Test
    void viserRiktigNesteUtbetalingsperiodeNårArbeidsgiverHarFlereForfallsdatoerFremITid() {
        // Arrange
        var behandlingId = 887755L;

        var januar01 = LocalDate.of(2019, 1, 1);
        var januar31 = LocalDate.of(2019, 1, 31);
        var januar23 = LocalDate.of(2019, 1, 23);

        var februar01 = LocalDate.of(2019, 2, 1);
        var februar28 = LocalDate.of(2019, 2, 28);

        var simuleringKjørtDato = LocalDate.of(2019, 1, 17).atStartOfDay();

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(januar01, januar31, D, YTEL, FP, 523, januar23))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(januar01, januar31, D, YTEL, FPREF, 14875,
                                                januar31))
                                .medSimulertPostering(
                                        postering(februar01, februar28, D, YTEL, FPREF,
                                                25875, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        var simuleringResultat = optSimuleringDto.get().simuleringResultat();
        assertThat(simuleringResultat.ingenPerioderMedAvvik()).isTrue();
        assertThat(simuleringResultat.perioderPerMottaker()).hasSize(2);

        // Sjekker neste utbetalingsperiode for Bruker
        var mottakerBruker = simuleringResultat.perioderPerMottaker().get(0);
        assertThat(mottakerBruker.mottakerType()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.BRUKER);
        assertThat(mottakerBruker.nesteUtbPeriode().fom()).isEqualTo(januar01);
        assertThat(mottakerBruker.nesteUtbPeriode().tom()).isEqualTo(januar31);

        // Sjekker neste utbetalingsperiode for arbeidsgiver
        var mottakerArbgiv = simuleringResultat.perioderPerMottaker().get(1);
        assertThat(mottakerArbgiv.mottakerType()).isEqualTo(no.nav.foreldrepenger.kontrakter.simulering.resultat.kodeverk.MottakerType.ARBG_ORG);
        assertThat(mottakerArbgiv.nesteUtbPeriode().fom()).isEqualTo(februar01);
        assertThat(mottakerArbgiv.nesteUtbPeriode().tom()).isEqualTo(februar28);
    }

    @Test
    void ingenPerioderMedAvvikDersomKunFremtidigRefusjonTilArbeidsgiver() {
        // Arrange
        var behandlingId = 887755L;
        var februar01 = LocalDate.of(2019, 2, 1);
        var februar28 = LocalDate.of(2019, 2, 28);
        var simuleringKjørtDato = LocalDate.of(2019, 1, 17).atStartOfDay();

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(februar01, februar28, D, YTEL, FPREF,
                                                25875, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        var simuleringDto = optSimuleringDto.get();
        assertThat(simuleringDto.simuleringResultat().ingenPerioderMedAvvik()).isTrue();
    }

    @Test
    void henterSumFeilutbetaling() {
        // Arrange
        var behandlingId = 887755L;
        var februar01 = LocalDate.of(2019, 2, 1);
        var februar28 = LocalDate.of(2019, 2, 28);
        var simuleringKjørtDato = LocalDate.of(2019, 3, 5).atStartOfDay();
        var feilutbetaltBeløp = 12000;

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(februar01, februar28, D, FEIL, FP,
                                                feilutbetaltBeløp, februar28))
                                .medSimulertPostering(
                                        postering(februar01, februar28, K, YTEL, FP, 30000,
                                                februar28))
                                .medSimulertPostering(
                                        postering(februar01, februar28, D, YTEL, FP, 18000,
                                                februar28))
                                .medSimulertPostering(postering(februar01, februar28, D, YTEL, FP,
                                        feilutbetaltBeløp, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        var feilutbetaltePerioderDto = simuleringResultatTjeneste.hentFeilutbetaltePerioder(behandlingId);

        // Assert
        assertThat(feilutbetaltePerioderDto.sumFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
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

    private SimulertPostering postering(LocalDate fom,
                                        LocalDate tom,
                                        BetalingType betalingType,
                                        PosteringType posteringType,
                                        Fagområde fagOmrådeKode,
                                        double beløp) {
        return postering(fom, tom, betalingType, posteringType, fagOmrådeKode, beløp, LocalDate.now());
    }

    private SimulertPostering postering(LocalDate fom,
                                        LocalDate tom,
                                        BetalingType betalingType,
                                        PosteringType posteringType,
                                        Fagområde fagOmrådeKode,
                                        double beløp,
                                        LocalDate forfallsdato) {
        return SimulertPostering.builder()
                .medFagOmraadeKode(fagOmrådeKode)
                .medFom(fom)
                .medTom(tom)
                .medBetalingType(betalingType)
                .medPosteringType(posteringType)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medForfallsdato(forfallsdato)
                .build();
    }
}
