package no.nav.foreldrepenger.oppdrag.iverksett;

import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.AKTØR_ID;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.ARBEIDSGIVER_ORG_NR;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.BEHANDLING_ID;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.ENDRINGSDATO;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.SAKSNUMMER;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.VEDTAKSDATO;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Inntektskategori;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseAndel;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseBehandlingInfo;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelsePeriode;

public class TilkjentYtelseV1MapperTest {

    @Test
    public void skal_map_fra_tilkjent_ytelse_V1_til_tilkjent_ytelse_entitet() {
        //Arrange
        TilkjentYtelseV1 tilkjentYtelseV1 = TilkjentYtelseTestDataHelper.opprettTilkjentYtelseV1();

        //Act
        TilkjentYtelseEntitet tilkjentYtelseEntitet = TilkjentYtelseV1Mapper.mapTilEntiteter(tilkjentYtelseV1);

        //Assert
        verifiserTilkjentYtelseEntitet(tilkjentYtelseEntitet);
        verifiserTilkjentYtelseBehandlingInfo(tilkjentYtelseEntitet.getTilkjentYtelseBehandlingInfo());
        verifiserTilkjentYtelsePeriode(tilkjentYtelseEntitet.getTilkjentYtelsePeriodeListe());
        List<TilkjentYtelseAndel> tyAndeler = tilkjentYtelseEntitet.getTilkjentYtelsePeriodeListe()
                .stream()
                .flatMap(periode -> periode.getTilkjentYtelseAndelListe().stream())
                .collect(Collectors.toList());
        verifisertTilkjentYtelseAndel(tyAndeler);
    }

    private void verifisertTilkjentYtelseAndel(List<TilkjentYtelseAndel> tyAndeler) {
        for (TilkjentYtelseAndel andel : tyAndeler) {
            boolean gjelderPeriode1 = andel.getTilkjentYtelsePeriode().getTilkjentYtelsePeriodeFom().equals(LocalDate.of(2018, 12, 24));
            if (gjelderPeriode1) {
                assertThat(andel.getArbeidsgiverOrgNr()).isEqualTo(ARBEIDSGIVER_ORG_NR);
                assertThat(andel.getArbeidsgiverAktørId()).isNull();
                assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
                assertThat(andel.getUtbetalingsgrad()).isEqualTo(BigDecimal.valueOf(100));
                assertThat(andel.getSatsBeløp()).isEqualTo(andel.getUtbetalesTilBruker() ? 100L : 1000L);
                assertThat(andel.getSatsType()).isEqualTo("DAGSATS");
                assertThat(andel.getTilkjentYtelseFeriepenger()).isEmpty();
            } else {
                if (andel.getUtbetalesTilBruker()) {
                    assertThat(andel.getArbeidsgiverOrgNr()).isNull();
                    assertThat(andel.getArbeidsgiverAktørId()).isNull();
                    assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
                    assertThat(andel.getUtbetalingsgrad()).isEqualTo(BigDecimal.valueOf(100));
                    assertThat(andel.getSatsBeløp()).isEqualTo(135L);
                    assertThat(andel.getSatsType()).isEqualTo("DAGSATS");
                    assertThat(andel.getTilkjentYtelseFeriepenger()).isEmpty();
                } else {
                    assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
                    assertThat(andel.getArbeidsgiverOrgNr()).isEqualTo(ARBEIDSGIVER_ORG_NR);
                    assertThat(andel.getArbeidsgiverAktørId()).isNull();
                    assertThat(andel.getSatsBeløp()).isEqualTo(1586L);
                    assertThat(andel.getTilkjentYtelseFeriepenger()).isNotEmpty();
                    assertThat(andel.getTilkjentYtelseFeriepenger().get(0).getOpptjeningsår()).isEqualTo(2018);
                    assertThat(andel.getTilkjentYtelseFeriepenger().get(0).getÅrsbeløp()).isEqualTo(187);
                }
            }
        }
    }

    private void verifiserTilkjentYtelsePeriode(List<TilkjentYtelsePeriode> tilkjentYtelsePeriodeList) {
        assertThat(tilkjentYtelsePeriodeList).hasSize(2);
        assertThat(tilkjentYtelsePeriodeList.get(0).getTilkjentYtelseAndelListe()).isNotEmpty();
        assertThat(tilkjentYtelsePeriodeList.get(1).getTilkjentYtelseAndelListe()).isNotEmpty();
        assertThat(tilkjentYtelsePeriodeList.get(0).getTilkjentYtelsePeriodeFom()).isEqualTo(LocalDate.of(2018, 12, 24));
        assertThat(tilkjentYtelsePeriodeList.get(0).getTilkjentYtelsePeriodeTom()).isEqualTo(LocalDate.of(2019, 2, 28));
        assertThat(tilkjentYtelsePeriodeList.get(1).getTilkjentYtelsePeriodeFom()).isEqualTo(LocalDate.of(2019, 3, 1));
        assertThat(tilkjentYtelsePeriodeList.get(1).getTilkjentYtelsePeriodeTom()).isEqualTo(LocalDate.of(2019, 3, 31));
    }

    private void verifiserTilkjentYtelseBehandlingInfo(TilkjentYtelseBehandlingInfo tilkjentYtelseBehandlingInfo) {
        assertThat(tilkjentYtelseBehandlingInfo.getYtelseType()).isEqualTo("FORELDREPENGER");
        assertThat(tilkjentYtelseBehandlingInfo.getVedtaksdato()).isEqualTo(VEDTAKSDATO);
        assertThat(tilkjentYtelseBehandlingInfo.getGjelderAdopsjon()).isEqualTo(false);
        assertThat(tilkjentYtelseBehandlingInfo.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(tilkjentYtelseBehandlingInfo.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(tilkjentYtelseBehandlingInfo.getForrigeBehandlingId()).isNull();
    }

    private void verifiserTilkjentYtelseEntitet(TilkjentYtelseEntitet tilkjentYtelseEntitet) {
        assertThat(tilkjentYtelseEntitet.getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(tilkjentYtelseEntitet.getEndringsdato().get()).isEqualTo(ENDRINGSDATO);
        assertThat(tilkjentYtelseEntitet.getErOpphør()).isEqualTo(false);
        assertThat(tilkjentYtelseEntitet.getErOpphørEtterSkjæringstidspunktet()).isEqualTo(false);
        assertThat(tilkjentYtelseEntitet.getTilkjentYtelseBehandlingInfo()).isNotNull();
        assertThat(tilkjentYtelseEntitet.getTilkjentYtelsePeriodeListe()).isNotEmpty();
    }
}
