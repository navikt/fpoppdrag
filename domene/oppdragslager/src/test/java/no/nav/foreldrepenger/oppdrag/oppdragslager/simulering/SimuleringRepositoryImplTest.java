package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.kodeverk.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverk.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class SimuleringRepositoryImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private SimuleringRepository simuleringRepository = new SimuleringRepositoryImpl(repoRule.getEntityManager());

    private String aktørId = "000134";

    @Test
    public void lagrer_simulering_grunnlag() {
        // Arrange
        Long behandlingId = 1111L;
        LocalDate kjøresDato = LocalDate.of(2018, 9, 20);
        SimuleringGrunnlag simuleringGrunnlag = opprettGrunnlag(behandlingId, aktørId, kjøresDato);

        // Act
        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        repoRule.getRepository().flushAndClear();

        // Assert
        Optional<SimuleringGrunnlag> funnetGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        assertThat(funnetGrunnlag).isPresent();
        SimuleringGrunnlag lagretGrunnlag = funnetGrunnlag.get(); //NOSONAR
        SimuleringResultat simuleringResultat = lagretGrunnlag.getSimuleringResultat();
        assertThat(simuleringResultat).isNotNull();
        assertThat(simuleringResultat.getSimuleringMottakere()).hasSize(1);
        assertThat(simuleringResultat.getSimuleringMottakere().iterator().next().getSimulertePosteringer()).hasSize(1);
    }


    @Test
    public void setter_forrige_grunnlag_til_inaktivt_ved_lagring_av_nytt_grunnlag_på_samme_behandling() {
        Long behandlingId = 1234L;

        // Oppretter første simuleringsgrunnlag for behandling
        LocalDate førsteForfallsdato = LocalDate.of(2018, 11, 20);
        SimuleringGrunnlag simuleringGrunnlag = opprettGrunnlag(behandlingId, aktørId, førsteForfallsdato);
        simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        repoRule.getRepository().flushAndClear();

        Optional<SimuleringGrunnlag> førstelagretGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        assertThat(førstelagretGrunnlag).isPresent();

        // Oppretter neste simuleringsgrunnlag for behandling
        LocalDate andreForfallsDato = LocalDate.of(2018, 12, 15);
        simuleringRepository.lagreSimuleringGrunnlag(opprettGrunnlag(behandlingId, aktørId, andreForfallsDato));
        repoRule.getRepository().flushAndClear();

        Optional<SimuleringGrunnlag> andreLagretGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        assertThat(andreLagretGrunnlag).isPresent();
        Set<SimuleringMottaker> simuleringMottakere = andreLagretGrunnlag.get().getSimuleringResultat().getSimuleringMottakere();
        assertThat(simuleringMottakere).hasSize(1);
        assertThat(simuleringMottakere.iterator().next().getSimulertePosteringer()).hasSize(1);
        assertThat(simuleringMottakere.iterator().next().getSimulertePosteringer().iterator().next().getForfallsdato()).isEqualTo(andreForfallsDato);

        List<SimuleringGrunnlag> inaktiveGrunnlag = repoRule.getEntityManager().createQuery(
                "from SimuleringGrunnlag s" +
                        " where s.eksternReferanse.behandlingId = :behandlingId" +
                        " and s.aktiv = :aktiv", SimuleringGrunnlag.class)
                .setParameter("behandlingId", behandlingId)
                .setParameter("aktiv", false)
                .getResultList();

        assertThat(inaktiveGrunnlag).hasSize(1);
    }

    private SimuleringGrunnlag opprettGrunnlag(Long behandlingId, String aktørId, LocalDate forfallsdato) {
        return SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(aktørId)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medSimuleringResultat(SimuleringResultat
                        .builder()
                        .medSimuleringMottaker(SimuleringMottaker
                                .builder()
                                .medMottakerType(MottakerType.BRUKER)
                                .medSimulertPostering(SimulertPostering
                                        .builder()
                                        .medFom(forfallsdato.withDayOfMonth(1))
                                        .medTom(forfallsdato)
                                        .medBeløp(BigDecimal.valueOf(10000))
                                        .medBetalingType(BetalingType.DEBIT)
                                        .medFagOmraadeKode(FagOmrådeKode.FORELDREPENGER)
                                        .medPosteringType(PosteringType.YTELSE)
                                        .medKonto("konto")
                                        .medKlasseKode(KlasseKode.FPATAL)
                                        .medForfallsdato(forfallsdato)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}