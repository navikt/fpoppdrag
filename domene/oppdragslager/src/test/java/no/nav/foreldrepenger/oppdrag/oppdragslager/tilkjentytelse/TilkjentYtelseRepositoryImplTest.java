package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.kodeverk.Inntektskategori;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;
import no.nav.vedtak.util.FPDateUtil;

public class TilkjentYtelseRepositoryImplTest {

    private static final LocalDate DAGENSDATO = FPDateUtil.iDag();

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final TilkjentYtelseRepository tilkjentYtelseRepository = new TilkjentYtelseRepositoryImpl(repoRule.getEntityManager());


    @Test
    public void lagreOgHentTilkjentYtelse() {
        //Arrange
        TilkjentYtelseEntitet tilkjentYtelseOpprettet = opprettTilkjentYtelse(Optional.of(DAGENSDATO.minusDays(1)));

        //Act
        tilkjentYtelseRepository.lagre(tilkjentYtelseOpprettet);

        //Assert
        Long id = tilkjentYtelseOpprettet.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Optional<TilkjentYtelseEntitet> tilkjentYtelseLestOpt = tilkjentYtelseRepository.hentTilkjentYtelse(1L);
        assertThat(tilkjentYtelseLestOpt).isPresent();

        TilkjentYtelseEntitet tilkjentYtelseLest = tilkjentYtelseLestOpt.get();
        verifiserTilkjentYtelse(tilkjentYtelseOpprettet, tilkjentYtelseLest);
        verifiserBehandlingInfo(tilkjentYtelseOpprettet.getTilkjentYtelseBehandlingInfo(), tilkjentYtelseLest.getTilkjentYtelseBehandlingInfo());
        verifiserPerioder(tilkjentYtelseOpprettet, tilkjentYtelseLest);
    }

    private void verifiserBehandlingInfo(TilkjentYtelseBehandlingInfo behInfoOpprettet, TilkjentYtelseBehandlingInfo behInfoLest) {
        assertThat(behInfoOpprettet.getAktørId()).isEqualTo(behInfoLest.getAktørId());
        assertThat(behInfoOpprettet.getAnsvarligSaksbehandler()).isEqualTo(behInfoLest.getAnsvarligSaksbehandler());
        assertThat(behInfoOpprettet.getSaksnummer()).isEqualTo(behInfoLest.getSaksnummer());
        assertThat(behInfoOpprettet.getGjelderAdopsjon()).isEqualTo(behInfoLest.getGjelderAdopsjon());
        assertThat(behInfoOpprettet.getVedtaksdato()).isEqualTo(behInfoLest.getVedtaksdato());
        assertThat(behInfoOpprettet.getYtelseType()).isEqualTo(behInfoLest.getYtelseType());
    }

    private void verifiserTilkjentYtelse(TilkjentYtelseEntitet tilkjentYtelseOpprettet, TilkjentYtelseEntitet tilkjentYtelseLest) {
        assertThat(tilkjentYtelseLest.getBehandlingId()).isEqualTo(tilkjentYtelseOpprettet.getBehandlingId());
        assertThat(tilkjentYtelseLest.getEndringsdato().get()).isEqualTo(tilkjentYtelseOpprettet.getEndringsdato().get());
        assertThat(tilkjentYtelseLest.getErOpphør()).isEqualTo(tilkjentYtelseOpprettet.getErOpphør());
        assertThat(tilkjentYtelseLest.getErOpphørEtterSkjæringstidspunktet()).isEqualTo(tilkjentYtelseOpprettet.getErOpphørEtterSkjæringstidspunktet());
    }

    private void verifiserPerioder(TilkjentYtelseEntitet tilkjentYtelseOpprettet, TilkjentYtelseEntitet tilkjentYtelseLest) {
        List<TilkjentYtelsePeriode> perioderOpprettet = tilkjentYtelseOpprettet.getTilkjentYtelsePeriodeListe();
        List<TilkjentYtelsePeriode> perioderLest = tilkjentYtelseLest.getTilkjentYtelsePeriodeListe();
        assertThat(perioderOpprettet.size()).isEqualTo(perioderLest.size());
        for (int i = 0; i < perioderLest.size(); i++) {
            assertThat(perioderOpprettet.get(i)).isEqualTo(perioderLest.get(i));
            verifiserAndeler(perioderOpprettet.get(i), perioderLest.get(i));
        }
    }

    private void verifiserAndeler(TilkjentYtelsePeriode periodeOpprettet, TilkjentYtelsePeriode periodeLest) {
        List<TilkjentYtelseAndel> andelerOpprettet = periodeOpprettet.getTilkjentYtelseAndelListe();
        List<TilkjentYtelseAndel> andelerLest = periodeLest.getTilkjentYtelseAndelListe();
        for (int i = 0; i < andelerLest.size(); i++) {
            assertThat(andelerOpprettet.get(i)).isEqualTo(andelerLest.get(i));
            verifiserFeriepenger(andelerOpprettet.get(i), andelerLest.get(i));
        }
    }

    private void verifiserFeriepenger(TilkjentYtelseAndel andelLest, TilkjentYtelseAndel andelOpprettet) {
        assertThat(andelOpprettet.getTilkjentYtelseFeriepenger()).containsOnly(
                andelLest.getTilkjentYtelseFeriepenger().get(0),
                andelLest.getTilkjentYtelseFeriepenger().get(1)
        );
    }

    private TilkjentYtelseEntitet opprettTilkjentYtelse(Optional<LocalDate> endringsdatoOpt) {
        TilkjentYtelseEntitet.Builder builder = TilkjentYtelseEntitet.builder()
                .medBehandlingId(1L)
                .medErOpphør(false)
                .medErOpphørEtterStp(false);
        endringsdatoOpt.ifPresent(builder::medEndringsdato);
        TilkjentYtelseEntitet tilkjentYtelse = builder.build();
        opprettTilkjentYtelseBehandlingInfo(tilkjentYtelse);
        opprettTilkjentYtelsePeriode(tilkjentYtelse);

        return tilkjentYtelse;
    }

    private void opprettTilkjentYtelseBehandlingInfo(TilkjentYtelseEntitet tilkjentYtelse) {
        TilkjentYtelseBehandlingInfo.builder()
                .medAktørId("9876543212345")
                .medAnsvarligSaksbehandler("Saksbehandler")
                .medGjelderAdopsjon(false)
                .medSaksnummer("333444555")
                .medVedtaksdato(DAGENSDATO)
                .medYtelseType(YtelseType.FORELDREPENGER.name())
                .build(tilkjentYtelse);
    }

    private void opprettTilkjentYtelsePeriode(TilkjentYtelseEntitet tilkjentYtelse) {
        TilkjentYtelsePeriode tyPeriode = TilkjentYtelsePeriode.builder()
                .medTilkjentYtelsePeriodeFomOgTom(LocalDate.now(), LocalDate.now().plusMonths(3))
                .build(tilkjentYtelse);

        opprettTilkjentYtelseAndel(tyPeriode);
    }

    private void opprettTilkjentYtelseAndel(TilkjentYtelsePeriode tyPeriode) {
        TilkjentYtelseAndel tyAndel = TilkjentYtelseAndel.builder()
                .medArbeidsgiverOrgNr("123456789")
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medSatsBeløp(2000L)
                .medSatsType(SatsType.DAGSATS.name())
                .medUtbetalesTilBruker(true)
                .medUtbetalingsgrad(new BigDecimal(100))
                .build(tyPeriode);

        opprettTilkjentYtelseFeriepenger(tyAndel);
        opprettTilkjentYtelseFeriepengerAnnetÅr(tyAndel);
    }

    private void opprettTilkjentYtelseFeriepenger(TilkjentYtelseAndel tyAndel) {
        TilkjentYtelseFeriepenger.builder()
                .medÅrsbeløp(10000L)
                .medOpptjeningsår(DAGENSDATO.getYear())
                .build(tyAndel);
    }

    private void opprettTilkjentYtelseFeriepengerAnnetÅr(TilkjentYtelseAndel tyAndel) {
        TilkjentYtelseFeriepenger.builder()
                .medÅrsbeløp(30000L)
                .medOpptjeningsår(DAGENSDATO.getYear()-1)
                .build(tyAndel);
    }
}