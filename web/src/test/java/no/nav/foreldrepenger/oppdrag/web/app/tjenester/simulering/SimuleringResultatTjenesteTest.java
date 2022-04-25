package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.DEBIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.KREDIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.FORELDREPENGER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.FORELDREPENGER_ARBEIDSGIVER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEILUTBETALING;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FORSKUDSSKATT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTELSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
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
public class SimuleringResultatTjenesteTest {

    private SimuleringRepository simuleringRepository;

    private HentNavnTjeneste hentNavnTjeneste = Mockito.mock(HentNavnTjeneste.class);
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private SimuleringResultatTjeneste simuleringResultatTjeneste;

    private String aktørId = "0";

    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository, hentNavnTjeneste,
                simuleringBeregningTjeneste);
    }

    @Test
    public void henter_simulering_resultat_etterbetaling_en_mottaker_ett_fagområde() {
        // Arrange
        Long behandlingId = 123L;
        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        LocalDate andreMåned = startDato.plusMonths(1);
        LocalDate nesteMåned = nesteForfallsdato.withDayOfMonth(1);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // 2 måneder siden, kr 2000 i etterbetaling
                                .medSimulertPostering(
                                        postering(startDato, startDato.plusDays(15), KREDIT, YTELSE, FORELDREPENGER,
                                                3000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(16), andreMåned.minusDays(1), KREDIT, YTELSE,
                                                FORELDREPENGER, 2000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), DEBIT, YTELSE, FORELDREPENGER,
                                                7000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), DEBIT, FORSKUDSSKATT,
                                                FORELDREPENGER, 100))
                                // Forrige måned, kr 4000 i etterbetaling
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), KREDIT, YTELSE, FORELDREPENGER,
                                                6000))
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), DEBIT, YTELSE, FORELDREPENGER,
                                                10000))
                                .medSimulertPostering(
                                        postering(andreMåned, nesteMåned.minusDays(1), DEBIT, FORSKUDSSKATT,
                                                FORELDREPENGER, 400))
                                // Neste måned, kr 10000 til utbetaling
                                .medSimulertPostering(
                                        postering(nesteMåned, nesteMåned.plusMonths(1).minusDays(1), DEBIT, YTELSE,
                                                FORELDREPENGER, 10000, nesteForfallsdato))
                                .medSimulertPostering(
                                        postering(nesteMåned, nesteMåned.plusMonths(1).minusDays(1), DEBIT,
                                                FORSKUDSSKATT, FORELDREPENGER, 400, nesteForfallsdato))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();

        assertThat(simuleringResultatDto.isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.getPeriodeFom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.getPeriodeTom()).isEqualTo(nesteMåned.minusDays(1));
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(6000);
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isEqualTo(0);
        assertThat(simuleringResultatDto.getSumInntrekk()).isEqualTo(0);
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);

        SimuleringForMottakerDto mottakerDto = simuleringResultatDto.getPerioderPerMottaker().get(0);
        assertThat(mottakerDto.getMottakerType()).isEqualTo(MottakerType.BRUKER);

        assertThat(mottakerDto.getResultatOgMotregningRader()).hasSize(2);

        // Inntrekk - skal være sortert i riktig rekkefølge
        SimuleringResultatRadDto inntrekk = mottakerDto.getResultatOgMotregningRader().get(0);
        assertThat(inntrekk.getFeltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekk.getResultaterPerMåned()).hasSize(3);
        assertThat(inntrekk.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(0);
        assertThat(inntrekk.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(0);
        assertThat(inntrekk.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(0);

        // Resultat
        SimuleringResultatRadDto resultat = mottakerDto.getResultatOgMotregningRader().get(1);
        assertThat(resultat.getFeltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(resultat.getResultaterPerMåned()).hasSize(3);
        assertThat(resultat.getResultaterPerMåned().get(0).getBeløp()).isEqualTo(2000);
        assertThat(resultat.getResultaterPerMåned().get(1).getBeløp()).isEqualTo(4000);
        assertThat(resultat.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(10000);


        assertThat(mottakerDto.getResultatPerFagområde()).hasSize(1);
        assertThat(mottakerDto.getResultatPerFagområde().get(0).getFagOmrådeKode()).isEqualTo(
                FagOmrådeKode.FORELDREPENGER);
        assertThat(mottakerDto.getResultatPerFagområde().get(0).getRader()).hasSize(3);

        // Nytt beløp - skal være sortert i riktig rekkefølge
        SimuleringResultatRadDto nyttBeløp = mottakerDto.getResultatPerFagområde().get(0).getRader().get(0);
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
        SimuleringResultatRadDto tidligereUtbetalt = mottakerDto.getResultatPerFagområde().get(0).getRader().get(1);
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
        assertThat(tidligereUtbetalt.getResultaterPerMåned().get(2).getBeløp()).isEqualTo(0);


        // Differanse
        SimuleringResultatRadDto differanse = mottakerDto.getResultatPerFagområde().get(0).getRader().get(2);
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
    public void henterSimuleringResultatFlereMottakere() {
        // Arrange
        Long behandlingId = 123L;
        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        LocalDate andreMåned = startDato.plusMonths(1);

        String fnrArbgiv = "24069305608";
        String navn = "Onkel Skrue";
        AktørId aktørIdArbgiver = new AktørId("1111111111111");

        String orgnr = "973861778";
        String orgName = "STATOIL ASA AVD STATOIL SOKKELVIRKSOMHET";
        when(hentNavnTjeneste.hentAktørIdGittFnr(fnrArbgiv)).thenReturn(aktørIdArbgiver);
        when(hentNavnTjeneste.hentNavnGittFnr(fnrArbgiv)).thenReturn(navn);
        when(hentNavnTjeneste.hentNavnGittOrgnummer(orgnr)).thenReturn(orgName);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // 2 måneder siden, kr 2000 i etterbetaling
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), KREDIT, YTELSE, FORELDREPENGER,
                                                5000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), DEBIT, YTELSE, FORELDREPENGER,
                                                7000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_PRIV).medMottakerNummer("nummer")
                                .medMottakerNummer(fnrArbgiv)
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), KREDIT, YTELSE,
                                        FORELDREPENGER_ARBEIDSGIVER, 1000))
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), DEBIT, YTELSE,
                                        FORELDREPENGER_ARBEIDSGIVER, 2000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medMottakerNummer(orgnr)
                                .medSimulertPostering(postering(startDato, andreMåned.minusDays(1), DEBIT, YTELSE,
                                        FORELDREPENGER_ARBEIDSGIVER, 1000))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();

        assertThat(simuleringResultatDto.isIngenPerioderMedAvvik()).isFalse();
        assertThat(simuleringResultatDto.getPeriodeFom()).isEqualTo(startDato);
        assertThat(simuleringResultatDto.getPeriodeTom()).isEqualTo(andreMåned.minusDays(1));
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isEqualTo(0);
        assertThat(simuleringResultatDto.getSumInntrekk()).isEqualTo(0);
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(2000);

        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(3);

        // Mottaker = bruker
        Optional<SimuleringForMottakerDto> brukerOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst();
        assertThat(brukerOptional).isPresent();
        SimuleringForMottakerDto bruker = brukerOptional.get();
        assertThat(bruker.getResultatPerFagområde()).hasSize(1);
        SimuleringResultatPerFagområdeDto perFagområdeDto = bruker.getResultatPerFagområde().get(0);
        assertThat(perFagområdeDto.getFagOmrådeKode()).isEqualTo(FagOmrådeKode.FORELDREPENGER);
        assertThat(perFagområdeDto.getRader()).hasSize(3);

        assertThat(bruker.getResultatOgMotregningRader()).hasSize(2);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getFeltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getResultaterPerMåned()).hasSize(1);
        assertThat(bruker.getResultatOgMotregningRader().get(0).getResultaterPerMåned().get(0).getBeløp()).isEqualTo(0);
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
        Optional<SimuleringForMottakerDto> arbgivPrivOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.ARBG_PRIV))
                .findFirst();
        assertThat(arbgivPrivOptional).isPresent();
        SimuleringForMottakerDto arbgivPriv = arbgivPrivOptional.get();
        assertThat(arbgivPriv.getMottakerNavn()).isEqualTo(navn);
        assertThat(arbgivPriv.getMottakerNummer()).isEqualTo(fnrArbgiv);
        assertThat(arbgivPriv.getMottakerIdentifikator()).isEqualTo(aktørIdArbgiver.getId());
        assertThat(arbgivPriv.getResultatPerFagområde()).hasSize(1);
        SimuleringResultatPerFagområdeDto perFagområdeDto1 = arbgivPriv.getResultatPerFagområde().get(0);

        assertThat(perFagområdeDto1.getFagOmrådeKode()).isEqualTo(FagOmrådeKode.FORELDREPENGER_ARBEIDSGIVER);
        assertThat(perFagområdeDto1.getRader()).hasSize(3);
        assertThat(arbgivPriv.getResultatOgMotregningRader()).hasSize(0);


        // Mottaker = arbeidsgiver med orgnr
        Optional<SimuleringForMottakerDto> arbgivOrgnrOptional = simuleringResultatDto.getPerioderPerMottaker()
                .stream()
                .filter(p -> p.getMottakerType().equals(MottakerType.ARBG_ORG))
                .findFirst();
        assertThat(arbgivOrgnrOptional).isPresent();
        SimuleringForMottakerDto arbgivOrgnr = arbgivOrgnrOptional.get();
        assertThat(arbgivOrgnr.getMottakerNavn()).isEqualToIgnoringCase(orgName);
        assertThat(arbgivOrgnr.getMottakerNummer()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.getMottakerIdentifikator()).isEqualTo(orgnr);
        assertThat(arbgivOrgnr.getResultatPerFagområde()).hasSize(1);
        SimuleringResultatPerFagområdeDto perFagområdeDto2 = arbgivOrgnr.getResultatPerFagområde().get(0);

        assertThat(perFagområdeDto2.getFagOmrådeKode()).isEqualTo(FagOmrådeKode.FORELDREPENGER_ARBEIDSGIVER);
        assertThat(perFagområdeDto2.getRader()).hasSize(1);
        assertThat(arbgivOrgnr.getResultatOgMotregningRader()).hasSize(0);
    }

    @Test
    public void finnerSumKunForFagområdeForeldrepengerOgHvisMottakerErBruker() {
        // Arrange
        Long behandlingId = 123L;
        LocalDate nesteForfallsdato = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate startDato = nesteForfallsdato.minusMonths(2).withDayOfMonth(1);
        LocalDate andreMåned = startDato.plusMonths(1);

        String fnrArbgiv = "24069305608";
        String navn = "Onkel Skrue";
        AktørId aktørIdArbgiver = new AktørId("1111111111111");

        String orgnr = "973861778";

        when(hentNavnTjeneste.hentAktørIdGittFnr(fnrArbgiv)).thenReturn(aktørIdArbgiver);
        when(hentNavnTjeneste.hentNavnGittFnr(fnrArbgiv)).thenReturn(navn);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), KREDIT, YTELSE, FORELDREPENGER,
                                                5000))
                                .medSimulertPostering(
                                        postering(startDato, andreMåned.minusDays(1), DEBIT, YTELSE, FORELDREPENGER,
                                                7000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_PRIV).medMottakerNummer("nummer")
                                .medMottakerNummer(fnrArbgiv)
                                .medSimulertPostering(
                                        postering(startDato.minusMonths(1), startDato.minusDays(1), KREDIT, YTELSE,
                                                FORELDREPENGER_ARBEIDSGIVER, 1000))
                                .medSimulertPostering(
                                        postering(startDato.minusMonths(1), startDato.minusDays(1), DEBIT, YTELSE,
                                                FORELDREPENGER_ARBEIDSGIVER, 2000))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medMottakerNummer(orgnr)
                                .medSimulertPostering(
                                        postering(andreMåned, andreMåned.plusMonths(1).minusDays(1), DEBIT, YTELSE,
                                                FORELDREPENGER_ARBEIDSGIVER, 1000))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);
        assertThat(simuleringDto).isPresent();

        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isEqualTo(0);
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(3);
    }

    @Test
    public void skalIkkeHaInntrekkOgNesteUtbetalingsperiodeVedEngangsstønad() {
        // Arrange
        Long behandlingId = 123456L;
        LocalDate datoKjøres = finnNesteForfallsdatoBasertPåDagensDato();
        LocalDate startDato = datoKjøres.minusMonths(1).withDayOfMonth(1);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.ENGANGSTØNAD)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), KREDIT, YTELSE,
                                                ENGANGSSTØNAD, 40000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), DEBIT, FEILUTBETALING,
                                                ENGANGSSTØNAD, 40000))
                                .medSimulertPostering(
                                        postering(startDato.plusDays(3), startDato.plusDays(3), DEBIT, YTELSE,
                                                ENGANGSSTØNAD, 40000))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        DetaljertSimuleringResultatDto simuleringResultatDto = simuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultatDto.getSumInntrekk()).isNull();
        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);
        assertThat(simuleringResultatDto.getPerioderPerMottaker().get(0).getNesteUtbPeriodeFom()).isNull();
        assertThat(simuleringResultatDto.getPerioderPerMottaker().get(0).getNestUtbPeriodeTom()).isNull();
    }

    @Test
    public void skalTrunkereDesimalerPåBeløp() {
        // Arrange
        Long behandlingId = 123456L;
        LocalDate førsteAugust2018 = LocalDate.of(2018, 8, 01);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(førsteAugust2018, førsteAugust2018.plusDays(15), DEBIT, YTELSE,
                                                FORELDREPENGER, 15600.75))
                                .medSimulertPostering(
                                        postering(førsteAugust2018.plusDays(16), førsteAugust2018.plusDays(25), DEBIT,
                                                YTELSE, FORELDREPENGER, 7400.85))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> simuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(simuleringDto).isPresent();
        assertThat(simuleringDto.get().getSimuleringResultat().getSumEtterbetaling()).isEqualTo(23001);
    }

    @Test
    public void viserRiktigNesteUtbetalingsperiodeNårArbeidsgiverHarFlereForfallsdatoerFremITid() {
        // Arrange
        Long behandlingId = 887755L;

        LocalDate januar01 = LocalDate.of(2019, 1, 1);
        LocalDate januar31 = LocalDate.of(2019, 1, 31);
        LocalDate januar23 = LocalDate.of(2019, 1, 23);

        LocalDate februar01 = LocalDate.of(2019, 2, 1);
        LocalDate februar28 = LocalDate.of(2019, 2, 28);

        LocalDateTime simuleringKjørtDato = LocalDate.of(2019, 1, 17).atStartOfDay();

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(januar01, januar31, DEBIT, YTELSE, FORELDREPENGER, 523, januar23))
                                .build())
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(januar01, januar31, DEBIT, YTELSE, FORELDREPENGER_ARBEIDSGIVER, 14875,
                                                januar31))
                                .medSimulertPostering(
                                        postering(februar01, februar28, DEBIT, YTELSE, FORELDREPENGER_ARBEIDSGIVER,
                                                25875, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        DetaljertSimuleringResultatDto simuleringResultat = optSimuleringDto.get().getSimuleringResultat();
        assertThat(simuleringResultat.isIngenPerioderMedAvvik()).isTrue();
        assertThat(simuleringResultat.getPerioderPerMottaker()).hasSize(2);

        // Sjekker neste utbetalingsperiode for Bruker
        SimuleringForMottakerDto mottakerBruker = simuleringResultat.getPerioderPerMottaker().get(0);
        assertThat(mottakerBruker.getMottakerType()).isEqualTo(MottakerType.BRUKER);
        assertThat(mottakerBruker.getNesteUtbPeriodeFom()).isEqualTo(januar01);
        assertThat(mottakerBruker.getNestUtbPeriodeTom()).isEqualTo(januar31);

        // Sjekker neste utbetalingsperiode for arbeidsgiver
        SimuleringForMottakerDto mottakerArbgiv = simuleringResultat.getPerioderPerMottaker().get(1);
        assertThat(mottakerArbgiv.getMottakerType()).isEqualTo(MottakerType.ARBG_ORG);
        assertThat(mottakerArbgiv.getNesteUtbPeriodeFom()).isEqualTo(februar01);
        assertThat(mottakerArbgiv.getNestUtbPeriodeTom()).isEqualTo(februar28);
    }

    @Test
    public void ingenPerioderMedAvvikDersomKunFremtidigRefusjonTilArbeidsgiver() {
        // Arrange
        Long behandlingId = 887755L;
        LocalDate februar01 = LocalDate.of(2019, 2, 1);
        LocalDate februar28 = LocalDate.of(2019, 2, 28);
        LocalDateTime simuleringKjørtDato = LocalDate.of(2019, 1, 17).atStartOfDay();

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.ARBG_ORG).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(februar01, februar28, DEBIT, YTELSE, FORELDREPENGER_ARBEIDSGIVER,
                                                25875, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        Optional<SimuleringDto> optSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(
                behandlingId);

        // Assert
        assertThat(optSimuleringDto).isPresent();
        SimuleringDto simuleringDto = optSimuleringDto.get();
        assertThat(simuleringDto.getSimuleringResultat().isIngenPerioderMedAvvik()).isTrue();
    }

    @Test
    public void henterSumFeilutbetaling() {
        // Arrange
        Long behandlingId = 887755L;
        LocalDate februar01 = LocalDate.of(2019, 2, 1);
        LocalDate februar28 = LocalDate.of(2019, 2, 28);
        LocalDateTime simuleringKjørtDato = LocalDate.of(2019, 3, 5).atStartOfDay();

        int feilutbetaltBeløp = 12000;
        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medSimuleringKjørtDato(simuleringKjørtDato)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                .medSimulertPostering(
                                        postering(februar01, februar28, DEBIT, FEILUTBETALING, FORELDREPENGER,
                                                feilutbetaltBeløp, februar28))
                                .medSimulertPostering(
                                        postering(februar01, februar28, KREDIT, YTELSE, FORELDREPENGER, 30000,
                                                februar28))
                                .medSimulertPostering(
                                        postering(februar01, februar28, DEBIT, YTELSE, FORELDREPENGER, 18000,
                                                februar28))
                                .medSimulertPostering(postering(februar01, februar28, DEBIT, YTELSE, FORELDREPENGER,
                                        feilutbetaltBeløp, februar28))
                                .build())
                        .build())
                .build();

        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);

        // Act
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = simuleringResultatTjeneste.hentFeilutbetaltePerioder(
                behandlingId);

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
                                        FagOmrådeKode fagOmrådeKode,
                                        double beløp) {
        return postering(fom, tom, betalingType, posteringType, fagOmrådeKode, beløp, LocalDate.now());
    }

    private SimulertPostering postering(LocalDate fom,
                                        LocalDate tom,
                                        BetalingType betalingType,
                                        PosteringType posteringType,
                                        FagOmrådeKode fagOmrådeKode,
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
