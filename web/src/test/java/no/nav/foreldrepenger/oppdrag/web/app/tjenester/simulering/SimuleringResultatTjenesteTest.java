package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.D;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.K;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.REFUTG;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FPREF;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEIL;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.SKAT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
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
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.DetaljertSimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.RadId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringForMottakerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerFagområdeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatRadDto;

@ExtendWith(JpaExtension.class)
class SimuleringResultatTjenesteTest {

    private SimuleringRepository simuleringRepository;

    private final HentNavnTjeneste hentNavnTjeneste = Mockito.mock(HentNavnTjeneste.class);
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
        var simuleringResultatDto = simuleringDto.get().getSimuleringResultat();

        assertThat(simuleringResultatDto.isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.getPeriodeFom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.getPeriodeTom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(6000);
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.getSumInntrekk()).isZero();
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);

        var mottakerDto = simuleringResultatDto.getPerioderPerMottaker().get(0);
        assertThat(mottakerDto.getMottakerType()).isEqualTo(MottakerType.BRUKER);

        assertThat(mottakerDto.getResultatOgMotregningRader()).hasSize(2);

        // Inntrekk - skal være sortert i riktig rekkefølge
        var inntrekk = mottakerDto.getResultatOgMotregningRader().get(0);
        assertThat(inntrekk.getFeltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekk.getResultaterPerMåned()).hasSize(3);
        assertThat(inntrekk.getResultaterPerMåned().get(0).getBeløp()).isZero();
        assertThat(inntrekk.getResultaterPerMåned().get(1).getBeløp()).isZero();
        assertThat(inntrekk.getResultaterPerMåned().get(2).getBeløp()).isZero();

        // Resultat
        var resultat = mottakerDto.getResultatOgMotregningRader().get(1);
        assertThat(resultat.getFeltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(resultat.getResultaterPerMåned()).hasSize(3);
        assertThat(resultat.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(2000);
        assertThat(resultat.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(4000);
        assertThat(resultat.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(10000);


        assertThat(mottakerDto.getResultatPerFagområde()).hasSize(1);
        assertThat(mottakerDto.getResultatPerFagområde().get(0).getFagOmrådeKode()).isEqualTo(
                Fagområde.FP);
        assertThat(mottakerDto.getResultatPerFagområde().get(0).getRader()).hasSize(3);

        // Nytt beløp - skal være sortert i riktig rekkefølge
        var nyttBeløp = mottakerDto.getResultatPerFagområde().get(0).getRader().get(0);
        assertThat(nyttBeløp.getFeltnavn()).isEqualTo(RadId.NYTT_BELØP);
        assertThat(nyttBeløp.getResultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(nyttBeløp.getResultaterPerMåned().get(0).getPeriode().getFom()).isEqualTo(startDato);
        assertThat(nyttBeløp.getResultaterPerMåned().get(0).getPeriode().getTom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(nyttBeløp.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(7000);
        // Assert andre periode
        assertThat(nyttBeløp.getResultaterPerMåned().get(1).getPeriode().getFom()).isEqualTo(andreMåned);
        assertThat(nyttBeløp.getResultaterPerMåned().get(1).getPeriode().getTom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(nyttBeløp.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(10000);
        // Assert tredje periode
        assertThat(nyttBeløp.getResultaterPerMåned().get(2).getPeriode().getFom()).isEqualTo(nesteMåned);
        assertThat(nyttBeløp.getResultaterPerMåned().get(2).getPeriode().getTom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(nyttBeløp.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(10000);

        // Tidligere utbetalt
        var tidligereUtbetalt = mottakerDto.getResultatPerFagområde().get(0).getRader().get(1);
        assertThat(tidligereUtbetalt.getFeltnavn()).isEqualTo(RadId.TIDLIGERE_UTBETALT);
        assertThat(tidligereUtbetalt.getResultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(0).getPeriode().getFom()).isEqualTo(startDato);
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(0).getPeriode().getTom()).isEqualTo(
                andreMåned.minusDays(1));
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(5000);
        // Assert andre periode
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(1).getPeriode().getFom()).isEqualTo(andreMåned);
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(1).getPeriode().getTom()).isEqualTo(
                nesteMåned.minusDays(1));
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(6000);
        // Assert tredje periode
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(2).getPeriode().getFom()).isEqualTo(nesteMåned);
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(2).getPeriode().getTom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(2).getBeløp()).isZero();


        // Differanse
        var differanse = mottakerDto.getResultatPerFagområde().get(0).getRader().get(2);
        assertThat(differanse.getFeltnavn()).isEqualTo(RadId.DIFFERANSE);
        assertThat(differanse.getResultaterPerMåned()).hasSize(3);
        // Assert første periode (skal returneres sortert)
        assertThat(differanse.getResultaterPerMåned().get(0).getPeriode().getFom()).isEqualTo(startDato);
        assertThat(differanse.getResultaterPerMåned().get(0).getPeriode().getTom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(differanse.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(2000);
        // Assert andre periode
        assertThat(differanse.getResultaterPerMåned().get(1).getPeriode().getFom()).isEqualTo(andreMåned);
        assertThat(differanse.getResultaterPerMåned().get(1).getPeriode().getTom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(differanse.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(4000);
        // Assert tredje periode
        assertThat(differanse.getResultaterPerMåned().get(2).getPeriode().getFom()).isEqualTo(nesteMåned);
        assertThat(differanse.getResultaterPerMåned().get(2).getPeriode().getTom()).isEqualTo(
                nesteMåned.plusMonths(1).minusDays(1));
        assertThat(differanse.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(10000);
    }

    @Test
    void henterSimuleringResultatFlereMottakere() {
        // Arrange
        var behandlingId = 123L;
        var nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        var startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        var andreMåned = startDato.plusMonths(1);

        var fnrArbgiv = "12345678999";
        var navn = "Onkel Skrue";
        var aktørIdArbgiver = new AktørId("1111111111111");

        var orgnr = "999999999";
        var orgName = "TEST BEDRIFT AS";
        when(hentNavnTjeneste.hentAktørIdGittFnr(fnrArbgiv)).thenReturn(aktørIdArbgiver);
        when(hentNavnTjeneste.hentNavnGittFnr(fnrArbgiv)).thenReturn(navn);
        when(hentNavnTjeneste.hentNavnGittOrgnummer(orgnr)).thenReturn(orgName);

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
        var simuleringResultatDto = simuleringDto.get().getSimuleringResultat();

        assertThat(simuleringResultatDto.isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.getPeriodeFom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.getPeriodeTom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.getSumInntrekk()).isZero();
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(2000);

        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(3);

        // Mottaker = bruker
        var brukerOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst();
        assertThat(brukerOptional).isPresent();
        var bruker = brukerOptional.get();
        assertThat(bruker.getResultatPerFagområde()).hasSize(1);
        var perFagområdeDto = bruker.getResultatPerFagområde().get(0);
        assertThat(perFagområdeDto.getFagOmrådeKode()).isEqualTo(Fagområde.FP);
        assertThat(perFagområdeDto.getRader()).hasSize(3);

        assertThat(bruker.getResultatOgMotregningRader()).hasSize(2);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getFeltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getResultaterPerMåned()).hasSize(1);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getResultaterPerMåned().get(0).getBeløp()).isZero();
        assertThat(bruker.getResultatOgMotregningRader()
                .get(0)
                .getResultaterPerMåned()
                .get(0)
                .getPeriode()
                .getFom()).isEqualTo(startDato);
        assertThat(bruker.getResultatOgMotregningRader()
                .get(0)
                .getResultaterPerMåned()
                .get(0)
                .getPeriode()
                .getTom()).isEqualTo(andreMåned.minusDays(1));

        assertThat(bruker.getResultatOgMotregningRader().get(1).getFeltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(bruker.getResultatOgMotregningRader().get(1).getResultaterPerMåned()).hasSize(1);
        assertThat(bruker.getResultatOgMotregningRader().get(1).getResultaterPerMåned().get(0).getBeløp()).isEqualTo(
                2000);
        assertThat(bruker.getResultatOgMotregningRader()
                .get(1)
                .getResultaterPerMåned()
                .get(0)
                .getPeriode()
                .getFom()).isEqualTo(startDato);
        assertThat(bruker.getResultatOgMotregningRader()
                .get(1)
                .getResultaterPerMåned()
                .get(0)
                .getPeriode()
                .getTom()).isEqualTo(andreMåned.minusDays(1));


        // Mottaker = arbeidsgiver med fnr
        var  arbgivPrivOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.ARBG_PRIV))
                .findFirst();
        assertThat(arbgivPrivOptional).isPresent();
        var arbgivPriv = arbgivPrivOptional.get();
        assertThat(arbgivPriv.getMottakerNavn()).isEqualTo(navn);
        assertThat(arbgivPriv.getMottakerNummer()).isEqualTo(fnrArbgiv);
        assertThat(arbgivPriv.getMottakerIdentifikator()).isEqualTo(aktørIdArbgiver.getId());
        assertThat(arbgivPriv.getResultatPerFagområde()).hasSize(1);
        var perFagområdeDto1 = arbgivPriv.getResultatPerFagområde().get(0);

        assertThat(perFagområdeDto1.getFagOmrådeKode()).isEqualTo(Fagområde.FPREF);
        assertThat(perFagområdeDto1.getRader()).hasSize(3);
        assertThat(arbgivPriv.getResultatOgMotregningRader()).isEmpty();


        // Mottaker = arbeidsgiver med orgnr
        var arbgivOrgnrOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.ARBG_ORG))
                .findFirst();
        assertThat(arbgivOrgnrOptional).isPresent();
        var arbgivOrgnr = arbgivOrgnrOptional.get();
        assertThat(arbgivOrgnr.getMottakerNavn()).isEqualToIgnoringCase(orgName);
        assertThat(arbgivOrgnr.getMottakerNummer()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.getMottakerIdentifikator()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.getResultatPerFagområde()).hasSize(1);
        var perFagområdeDto2 = arbgivOrgnr.getResultatPerFagområde().get(0);

        assertThat(perFagområdeDto2.getFagOmrådeKode()).isEqualTo(Fagområde.FPREF);
        assertThat(perFagområdeDto2.getRader()).hasSize(1);
        assertThat(arbgivOrgnr.getResultatOgMotregningRader()).isEmpty();
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

        when(hentNavnTjeneste.hentAktørIdGittFnr(fnrArbgiv)).thenReturn(aktørIdArbgiver);
        when(hentNavnTjeneste.hentNavnGittFnr(fnrArbgiv)).thenReturn(navn);

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
        var simuleringResultatDto = simuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isZero();
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(3);
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
        assertThat(simuleringDto).isPresent();
        var simuleringResultatDto = simuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultatDto.getSumInntrekk()).isNull();
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);
        assertThat(simuleringResultatDto.getPerioderPerMottaker().get(0).getNesteUtbPeriodeFom()).isNull();
        assertThat(simuleringResultatDto.getPerioderPerMottaker().get(0).getNestUtbPeriodeTom()).isNull();
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
        assertThat(simuleringDto.get().getSimuleringResultat().getSumEtterbetaling()).isEqualTo(23001);
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
        var simuleringResultat = optSimuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultat.isIngenPerioderMedAvvik()).isTrue();
        assertThat(simuleringResultat.getPerioderPerMottaker()).hasSize(2);

        // Sjekker neste utbetalingsperiode for Bruker
        var mottakerBruker = simuleringResultat.getPerioderPerMottaker().get(0);
        assertThat(mottakerBruker.getMottakerType()).isEqualTo(MottakerType.BRUKER);
        assertThat(mottakerBruker.getNesteUtbPeriodeFom()).isEqualTo(januar01);
        assertThat(mottakerBruker.getNestUtbPeriodeTom()).isEqualTo(januar31);

        // Sjekker neste utbetalingsperiode for arbeidsgiver
        var mottakerArbgiv = simuleringResultat.getPerioderPerMottaker().get(1);
        assertThat(mottakerArbgiv.getMottakerType()).isEqualTo(MottakerType.ARBG_ORG);
        assertThat(mottakerArbgiv.getNesteUtbPeriodeFom()).isEqualTo(februar01);
        assertThat(mottakerArbgiv.getNestUtbPeriodeTom()).isEqualTo(februar28);
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
        assertThat(simuleringDto.getSimuleringResultat().isIngenPerioderMedAvvik()).isTrue();
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
        assertThat(feilutbetaltePerioderDto.getSumFeilutbetaling()).isEqualTo(feilutbetaltBeløp);
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
