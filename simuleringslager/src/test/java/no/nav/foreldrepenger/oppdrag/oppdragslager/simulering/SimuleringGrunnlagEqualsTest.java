package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

class SimuleringGrunnlagEqualsTest {



    @Test
    void sjekkOmSimuleringGrunnlagMedFlereMottakkereMenForskjelligRekkefølgeErLik() {
        var simuleringGrunnlag1 = opprettGrunnlag(123L,
                simuleringMottaker(BigDecimal.valueOf(250_00)),
                simuleringMottaker(BigDecimal.valueOf(250_000.00)));
        var simuleringGrunnlag2 = opprettGrunnlag(123L,
                simuleringMottaker(BigDecimal.valueOf(250_000.00)),
                simuleringMottaker(BigDecimal.valueOf(250_00)));
        assertThat(simuleringGrunnlag1).isEqualTo(simuleringGrunnlag2);
    }

    @Test
    void simuleringsGrunnlagMedForskjelligBehandlingIdErIkkeLike() {
        var simuleringGrunnlag1 = opprettGrunnlag(123L, simuleringMottaker(BigDecimal.valueOf(250_00)));
        var simuleringGrunnlag2 = opprettGrunnlag(456L, simuleringMottaker(BigDecimal.valueOf(250_000)));
        assertThat(simuleringGrunnlag1).isNotEqualTo(simuleringGrunnlag2);
    }

    @Test
    void simuleringsGrunnlagMedForskjelligAntallSimuleringMottakerErIkkeLike() {
        var simuleringGrunnlag1 = opprettGrunnlag(123L,
                simuleringMottaker(BigDecimal.valueOf(250_00)),
                simuleringMottaker(BigDecimal.valueOf(250_000.00)));
        var simuleringGrunnlag2 = opprettGrunnlag(123L,
                simuleringMottaker(BigDecimal.valueOf(250_00)));
        assertThat(simuleringGrunnlag1).isNotEqualTo(simuleringGrunnlag2);
    }

    @Test
    void simuleringsGrunnlagMedForskjelligAntallPosteringerErIkkeLike() {
        var simuleringGrunnlag1 = opprettGrunnlag(123L, simuleringMottaker(BigDecimal.valueOf(250_00), 1));
        var simuleringGrunnlag2 = opprettGrunnlag(123L, simuleringMottaker(BigDecimal.valueOf(250_00), 2));
        assertThat(simuleringGrunnlag1).isNotEqualTo(simuleringGrunnlag2);
    }

    @Test
    void simuleringsGrunnlagMedForskjelligBeløpPåPosteringErIkkeLike() {
        var simuleringGrunnlag1 = opprettGrunnlag(123L, simuleringMottaker(BigDecimal.valueOf(110_00)));
        var simuleringGrunnlag2 = opprettGrunnlag(123L, simuleringMottaker(BigDecimal.valueOf(250_00)));
        assertThat(simuleringGrunnlag1).isNotEqualTo(simuleringGrunnlag2);
    }

    private static SimuleringGrunnlag opprettGrunnlag(Long behandlingId, SimuleringMottaker... simuleringMottakere) {
        SimuleringResultat.Builder simuleringResultat = SimuleringResultat.builder();
        if (simuleringMottakere != null) {
            for (var simuleringMottaker : simuleringMottakere) {
                simuleringResultat.medSimuleringMottaker(simuleringMottaker);
            }
        }
        return SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId("123456789")
                .medYtelseType(YtelseType.FP)
                .medSimuleringResultat(simuleringResultat.build())
                .build();
    }

    private static SimuleringMottaker simuleringMottaker(BigDecimal beløp) {
        return simuleringMottaker(beløp, 1);
    }

    private static SimuleringMottaker simuleringMottaker(BigDecimal beløp, int antallPosteringer) {
        var simuleringMottaker = SimuleringMottaker.builder()
                .medMottakerType(MottakerType.BRUKER)
                .medMottakerNummer("test_nummer");
        for (int i=0; i < antallPosteringer; i++) {
            simuleringMottaker.medSimulertPostering(simulertPostering(beløp));
        }
        return simuleringMottaker.build();
    }



    private static SimulertPostering simulertPostering(BigDecimal beløp) {
        var forfallsdato = LocalDate.now();
        return SimulertPostering.builder()
                .medFom(forfallsdato.withDayOfMonth(1))
                .medTom(forfallsdato)
                .medBeløp(beløp)
                .medBetalingType(BetalingType.D)
                .medFagOmraadeKode(Fagområde.FP)
                .medPosteringType(PosteringType.YTEL)
                .medForfallsdato(forfallsdato)
                .build();
    }


}
