package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.D;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.K;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.FP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde.SP;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
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
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.KontraktFagområde;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.RadId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;

@ExtendWith(JpaExtension.class)
class SimuleringResultatTjenesteFlereYtelserTest {

    private SimuleringRepository simuleringRepository;

    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private SimuleringResultatTjeneste simuleringResultatTjeneste;

    private String aktørId = "0";

    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository, mock(PersonTjeneste.class), simuleringBeregningTjeneste);
    }

    @Test
    void henterResultatForFlereYtelser() {
        // Arrange
        var behandlingId = 123L;
        var august_01 = LocalDate.of(2018, 8, 1);
        var august_31 = august_01.plusMonths(1).minusDays(1);
        var september_01 = august_01.plusMonths(1);
        var september_30 = september_01.plusMonths(1).minusDays(1);
        var oktober_01 = september_01.plusMonths(1);
        var oktober_31 = oktober_01.plusMonths(1).minusDays(1);

        var simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Første måned, kun sykepenger
                                .medSimulertPostering(postering(august_01, august_31, K, YTEL, SP, 1000))
                                .medSimulertPostering(postering(august_01, august_31, D, YTEL, SP, 2000))
                                // Andre måned, foreldrepenger og sykepenger
                                .medSimulertPostering(postering(september_01, september_30, K, YTEL, FP, 2000))
                                .medSimulertPostering(postering(september_01, september_30, D, YTEL, FP, 7000))
                                .medSimulertPostering(postering(september_01, september_30, K, YTEL, SP, 3000))
                                .medSimulertPostering(postering(september_01, september_30, D, YTEL, SP, 4000))
                                // Tredje måned, kun foreldrepenger
                                .medSimulertPostering(postering(oktober_01, oktober_31, K, YTEL, FP, 6000))
                                .medSimulertPostering(postering(oktober_01, oktober_31, D, YTEL, FP, 10000))
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
        assertThat(simuleringResultatDto.periode().fom()).isEqualTo(august_01);
        assertThat(simuleringResultatDto.periode().tom()).isEqualTo(oktober_31);
        assertThat(simuleringResultatDto.sumEtterbetaling()).isEqualTo(9000); // Kun foreldrepenger
        assertThat(simuleringResultatDto.sumInntrekk()).isZero();
        assertThat(simuleringResultatDto.sumFeilutbetaling()).isZero();

        assertThat(simuleringResultatDto.perioderPerMottaker()).hasSize(1);
        var mottakerBruker = simuleringResultatDto.perioderPerMottaker().get(0);
        assertThat(mottakerBruker.resultatPerFagområde()).hasSize(2);

        // Resultat for foreldrepenger -- Skal være sortert slik at foreldrepenger kommer først
        var foreldrepenger = mottakerBruker.resultatPerFagområde().get(0);
        assertThat(foreldrepenger.fagOmrådeKode()).isEqualTo(KontraktFagområde.FP);
        assertThat(foreldrepenger.rader()).hasSize(3);
        assertThat(foreldrepenger.rader().get(0).resultaterPerMåned()).hasSize(2);

        assertThat(foreldrepenger.rader().get(0).resultaterPerMåned().get(0).periode().fom()).isEqualTo(september_01);
        assertThat(foreldrepenger.rader().get(0).resultaterPerMåned().get(1).periode().fom()).isEqualTo(oktober_01);


        // Resultat for sykepenger
        var sykepenger = mottakerBruker.resultatPerFagområde().get(1);
        assertThat(sykepenger.fagOmrådeKode()).isEqualTo(KontraktFagområde.SP);
        assertThat(sykepenger.rader()).hasSize(3);
        assertThat(sykepenger.rader().get(0).resultaterPerMåned()).hasSize(2);
        assertThat(sykepenger.rader().get(0).resultaterPerMåned().get(0).periode().fom()).isEqualTo(august_01);
        assertThat(sykepenger.rader().get(0).resultaterPerMåned().get(1).periode().fom()).isEqualTo(september_01);

        var resultatOgMotregningRader = mottakerBruker.resultatOgMotregningRader();
        assertThat(resultatOgMotregningRader).hasSize(3);
        var resEtterMotregning = resultatOgMotregningRader.get(0);
        assertThat(resEtterMotregning.feltnavn()).isEqualTo(RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resEtterMotregning.resultaterPerMåned()).hasSize(3);
        verifiserPeriode(resEtterMotregning.resultaterPerMåned().get(0), august_01, august_31, 1000);
        verifiserPeriode(resEtterMotregning.resultaterPerMåned().get(1), september_01, september_30, 6000);
        verifiserPeriode(resEtterMotregning.resultaterPerMåned().get(2), oktober_01, oktober_31, 4000);


        var inntrekk = resultatOgMotregningRader.get(1);
        assertThat(inntrekk.feltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekk.resultaterPerMåned()).hasSize(3);
        verifiserAtAlleBeløpEr0(inntrekk.resultaterPerMåned());

        var resultat = resultatOgMotregningRader.get(2);
        assertThat(resultat.feltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(resultat.resultaterPerMåned()).hasSize(3);
        verifiserPeriode(resultat.resultaterPerMåned().get(0), august_01, august_31, 1000);
        verifiserPeriode(resultat.resultaterPerMåned().get(1), september_01, september_30, 6000);
        verifiserPeriode(resultat.resultaterPerMåned().get(2), oktober_01, oktober_31, 4000);
    }

    private void verifiserPeriode(SimuleringDto.SimuleringResultatPerMånedDto resultatPerMånedDto, LocalDate forventetFom, LocalDate forventetTom, long forventetBeløp) {
        assertThat(resultatPerMånedDto.beløp()).isEqualTo(forventetBeløp);
        assertThat(resultatPerMånedDto.periode().fom()).isEqualTo(forventetFom);
        assertThat(resultatPerMånedDto.periode().tom()).isEqualTo(forventetTom);

    }

    private void verifiserAtAlleBeløpEr0(List<SimuleringDto.SimuleringResultatPerMånedDto> resultaterPerMåned) {
        for (SimuleringDto.SimuleringResultatPerMånedDto simuleringResultatPerMånedDto : resultaterPerMåned) {
            assertThat(simuleringResultatPerMånedDto.beløp()).isZero();
        }
    }

    private SimulertPostering postering(LocalDate fom, LocalDate tom, BetalingType betalingType, PosteringType posteringType,
                                        Fagområde fagOmrådeKode, int beløp) {
        return SimulertPostering.builder()
                .medFagOmraadeKode(fagOmrådeKode)
                .medFom(fom)
                .medTom(tom)
                .medBetalingType(betalingType)
                .medPosteringType(posteringType)
                .medBeløp(BigDecimal.valueOf(beløp))
                .medForfallsdato(tom)
                .build();
    }

}
