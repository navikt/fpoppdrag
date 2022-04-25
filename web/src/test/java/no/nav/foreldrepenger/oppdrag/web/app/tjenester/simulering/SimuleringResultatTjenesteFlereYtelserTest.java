package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;


import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.DEBIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType.KREDIT;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.FORELDREPENGER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode.SYKEPENGER;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.YTELSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
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
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.DetaljertSimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.RadId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringForMottakerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerFagområdeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatPerMånedDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatRadDto;

@ExtendWith(JpaExtension.class)
public class SimuleringResultatTjenesteFlereYtelserTest {

    private SimuleringRepository simuleringRepository;

    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private SimuleringResultatTjeneste simuleringResultatTjeneste;

    private String aktørId = "0";

    @BeforeEach
    void setUp(EntityManager entityManager) {
        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository, mock(HentNavnTjeneste.class), simuleringBeregningTjeneste);
    }

    @Test
    public void henterResultatForFlereYtelser() {
        // Arrange
        Long behandlingId = 123L;
        LocalDate august_01 = LocalDate.of(2018, 8, 1);
        LocalDate august_31 = august_01.plusMonths(1).minusDays(1);
        LocalDate september_01 = august_01.plusMonths(1);
        LocalDate september_30 = september_01.plusMonths(1).minusDays(1);
        LocalDate oktober_01 = september_01.plusMonths(1);
        LocalDate oktober_31 = oktober_01.plusMonths(1).minusDays(1);

        SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat.builder()
                        .medSimuleringMottaker(SimuleringMottaker.builder()
                                .medMottakerType(MottakerType.BRUKER).medMottakerNummer("nummer")
                                // Første måned, kun sykepenger
                                .medSimulertPostering(postering(august_01, august_31, KREDIT, YTELSE, SYKEPENGER, 1000))
                                .medSimulertPostering(postering(august_01, august_31, DEBIT, YTELSE, SYKEPENGER, 2000))
                                // Andre måned, foreldrepenger og sykepenger
                                .medSimulertPostering(postering(september_01, september_30, KREDIT, YTELSE, FORELDREPENGER, 2000))
                                .medSimulertPostering(postering(september_01, september_30, DEBIT, YTELSE, FORELDREPENGER, 7000))
                                .medSimulertPostering(postering(september_01, september_30, KREDIT, YTELSE, SYKEPENGER, 3000))
                                .medSimulertPostering(postering(september_01, september_30, DEBIT, YTELSE, SYKEPENGER, 4000))
                                // Tredje måned, kun foreldrepenger
                                .medSimulertPostering(postering(oktober_01, oktober_31, KREDIT, YTELSE, FORELDREPENGER, 6000))
                                .medSimulertPostering(postering(oktober_01, oktober_31, DEBIT, YTELSE, FORELDREPENGER, 10000))
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
        assertThat(simuleringResultatDto.getPeriodeFom()).isEqualTo(august_01);
        assertThat(simuleringResultatDto.getPeriodeTom()).isEqualTo(oktober_31);
        assertThat(simuleringResultatDto.getSumEtterbetaling()).isEqualTo(9000); // Kun foreldrepenger
        assertThat(simuleringResultatDto.getSumInntrekk()).isEqualTo(0);
        assertThat(simuleringResultatDto.getSumFeilutbetaling()).isEqualTo(0);

        assertThat(simuleringResultatDto.getPerioderPerMottaker()).hasSize(1);
        SimuleringForMottakerDto mottakerBruker = simuleringResultatDto.getPerioderPerMottaker().get(0);
        assertThat(mottakerBruker.getResultatPerFagområde()).hasSize(2);

        // Resultat for foreldrepenger -- Skal være sortert slik at foreldrepenger kommer først
        SimuleringResultatPerFagområdeDto foreldrepenger = mottakerBruker.getResultatPerFagområde().get(0);
        assertThat(foreldrepenger.getFagOmrådeKode()).isEqualTo(FagOmrådeKode.FORELDREPENGER);
        assertThat(foreldrepenger.getRader()).hasSize(3);
        assertThat(foreldrepenger.getRader().get(0).getResultaterPerMåned()).hasSize(2);
        assertThat(foreldrepenger.getRader().get(0).getResultaterPerMåned().get(0).getPeriode().getFom()).isEqualTo(september_01);
        assertThat(foreldrepenger.getRader().get(0).getResultaterPerMåned().get(1).getPeriode().getFom()).isEqualTo(oktober_01);


        // Resultat for sykepenger
        SimuleringResultatPerFagområdeDto sykepenger = mottakerBruker.getResultatPerFagområde().get(1);
        assertThat(sykepenger.getFagOmrådeKode()).isEqualTo(FagOmrådeKode.SYKEPENGER);
        assertThat(sykepenger.getRader()).hasSize(3);
        assertThat(sykepenger.getRader().get(0).getResultaterPerMåned()).hasSize(2);
        assertThat(sykepenger.getRader().get(0).getResultaterPerMåned().get(0).getPeriode().getFom()).isEqualTo(august_01);
        assertThat(sykepenger.getRader().get(0).getResultaterPerMåned().get(1).getPeriode().getFom()).isEqualTo(september_01);

        List<SimuleringResultatRadDto> resultatOgMotregningRader = mottakerBruker.getResultatOgMotregningRader();
        assertThat(resultatOgMotregningRader).hasSize(3);
        SimuleringResultatRadDto resEtterMotregning = resultatOgMotregningRader.get(0);
        assertThat(resEtterMotregning.getFeltnavn()).isEqualTo(RadId.RESULTAT_ETTER_MOTREGNING);
        assertThat(resEtterMotregning.getResultaterPerMåned()).hasSize(3);
        verifiserPeriode(resEtterMotregning.getResultaterPerMåned().get(0), august_01, august_31, 1000);
        verifiserPeriode(resEtterMotregning.getResultaterPerMåned().get(1), september_01, september_30, 6000);
        verifiserPeriode(resEtterMotregning.getResultaterPerMåned().get(2), oktober_01, oktober_31, 4000);


        SimuleringResultatRadDto inntrekk = resultatOgMotregningRader.get(1);
        assertThat(inntrekk.getFeltnavn()).isEqualTo(RadId.INNTREKK_NESTE_MÅNED);
        assertThat(inntrekk.getResultaterPerMåned()).hasSize(3);
        verifiserAtAlleBeløpEr0(inntrekk.getResultaterPerMåned());

        SimuleringResultatRadDto resultat = resultatOgMotregningRader.get(2);
        assertThat(resultat.getFeltnavn()).isEqualTo(RadId.RESULTAT);
        assertThat(resultat.getResultaterPerMåned()).hasSize(3);
        verifiserPeriode(resultat.getResultaterPerMåned().get(0), august_01, august_31, 1000);
        verifiserPeriode(resultat.getResultaterPerMåned().get(1), september_01, september_30, 6000);
        verifiserPeriode(resultat.getResultaterPerMåned().get(2), oktober_01, oktober_31, 4000);
    }

    private void verifiserPeriode(SimuleringResultatPerMånedDto resultatPerMånedDto, LocalDate forventetFom, LocalDate forventetTom, long forventetBeløp) {
        assertThat(resultatPerMånedDto.getBeløp()).isEqualTo(forventetBeløp);
        assertThat(resultatPerMånedDto.getPeriode().getFom()).isEqualTo(forventetFom);
        assertThat(resultatPerMånedDto.getPeriode().getTom()).isEqualTo(forventetTom);

    }

    private void verifiserAtAlleBeløpEr0(List<SimuleringResultatPerMånedDto> resultaterPerMåned) {
        for (SimuleringResultatPerMånedDto simuleringResultatPerMånedDto : resultaterPerMåned) {
            assertThat(simuleringResultatPerMånedDto.getBeløp()).isEqualTo(0);
        }
    }

    private SimulertPostering postering(LocalDate fom, LocalDate tom, BetalingType betalingType, PosteringType posteringType,
                                        FagOmrådeKode fagOmrådeKode, int beløp) {
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
