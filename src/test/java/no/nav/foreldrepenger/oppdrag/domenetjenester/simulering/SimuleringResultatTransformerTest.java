package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDetaljerDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningsPeriodeDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosSimuleringRespons;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

class SimuleringResultatTransformerTest {

    PersonTjeneste tpsTjeneste = mock(PersonTjeneste.class);
    SimuleringResultatTransformer simuleringResultatTransformer = new SimuleringResultatTransformer(tpsTjeneste);

    @Test
    void utlederMottakerTypeBruker() {
        var mottakerId = "12345678910";

        var mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, true);
        assertThat(mottakerType).isEqualTo(MottakerType.BRUKER);
    }

    @Test
    void utlederMottakerTypeARBG_ORG() {
        var mottakerId = "0099999999";

        var mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, false);
        assertThat(mottakerType).isEqualTo(MottakerType.ARBG_ORG);
    }

    @Test
    void utlederMottakerTypeARBG_PRIV() {
        var mottakerId = "12345678910";

        var mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, false);
        assertThat(mottakerType).isEqualTo(MottakerType.ARBG_PRIV);
    }

    @Test
    void utlederBetalingTypeDebit() {
        var betalingTypeBeløp = simuleringResultatTransformer.utledBetalingType(BigDecimal.valueOf(1));
        assertThat(betalingTypeBeløp).isEqualTo(BetalingType.D);
    }


    @Test
    void utlederBetalingTypeKredit() {
        var betalingTypeBeløp = simuleringResultatTransformer.utledBetalingType(BigDecimal.valueOf(-1));
        assertThat(betalingTypeBeløp).isEqualTo(BetalingType.K);
    }

    @Test
    void returnererStrippetOrgnrHvisIkkeFnr() {
        var mottattOrgnr = "0099999999";
        var strippetOrgnr = "99999999";

        var orgnr = simuleringResultatTransformer.hentAktørIdHvisFnr(mottattOrgnr);
        assertThat(orgnr).isEqualTo(strippetOrgnr);
    }

    @Test
    void returnererAktørIdHvisFnr() {
        var fnr = "12345678910";
        var aktørId = new AktørId("1234567890123");

        when(tpsTjeneste.hentAktørForFnr(fnr)).thenReturn(Optional.of(aktørId));

        String resultat = simuleringResultatTransformer.hentAktørIdHvisFnr(fnr);
        assertThat(resultat).isEqualTo(aktørId.getId());
    }

    @Test
    void sokosOgSoapGirSammeGrunnlagMedFlerePerioderMottakereOgDuplikater() {
        when(tpsTjeneste.hentAktørForFnr("12345678910")).thenReturn(Optional.of(new AktørId("1234567890123")));
        var januar = "2026-01-31";
        var februar = "2026-02-28";
        var soap = new BeregningDto.Builder().gjelderId("12345678910").beregningsPeriode(List.of(
            new BeregningsPeriodeDto("2026-01-01", januar, List.of(
                soapStoppnivå("12345678910", "FP", januar, "100", "100", "-20"),
                soapStoppnivå("00999999999", "FPREF", januar, "500"))),
            new BeregningsPeriodeDto("2026-02-01", februar, List.of(soapStoppnivå("12345678910", "FP", februar, "200"))))).build();
        var sokos = new SokosSimuleringRespons.Beregning("12345678910  ", List.of(
            sokosPeriode("12345678910 ", "FP ", februar, "200.00"),
            sokosPeriode("00999999999 ", "FPREF ", januar, "500.00"),
            sokosPeriode("12345678910 ", "FP ", januar, "-20.00", "100.00", "100.0")));

        var mottakere = new HashMap<String, SimuleringMottaker.Builder>();
        simuleringResultatTransformer.mapSimulering(mottakere, soap);
        var resultat = SimuleringResultat.builder();
        mottakere.values().stream().map(SimuleringMottaker.Builder::build).forEach(resultat::medSimuleringMottaker);
        var fasit = SimuleringGrunnlag.builder().medEksternReferanse(new BehandlingRef(42L))
            .medAktørId("1234567890123").medYtelseType(YtelseType.FP).medSimuleringResultat(resultat.build()).build();

        var grunnlag = simuleringResultatTransformer.mapSokosSimulering(42L, List.of(sokos), YtelseType.FP);
        assertThat(grunnlag).isEqualTo(fasit);
        assertThat(grunnlag.getSimuleringResultat().getSimuleringMottakere()).hasSize(2);

        simuleringResultatTransformer.mapSimuleringUtenInntrekk(soap, fasit);
        assertThat(grunnlag).isNotEqualTo(fasit);
        simuleringResultatTransformer.mapSokosSimuleringUtenInntrekk(sokos, grunnlag);
        assertThat(grunnlag).isEqualTo(fasit);
    }

    private static BeregningStoppnivåDto soapStoppnivå(String mottaker, String fagområde, String tom, String... beløp) {
        return new BeregningStoppnivåDto.Builder().utbetalesTilId(mottaker).kodeFagomraade(fagområde).forfall(tom)
            .beregningStoppnivaaDetaljer(List.of(beløp).stream().map(b -> new BeregningStoppnivåDetaljerDto.Builder()
                .faktiskFom(tom.substring(0, 8) + "01").faktiskTom(tom).belop(new BigDecimal(b)).typeKlasse("YTEL").build()).toList()).build();
    }

    private static SokosSimuleringRespons.PeriodeStoppnivaa sokosPeriode(String mottaker, String fagområde, String tom, String... beløp) {
        return new SokosSimuleringRespons.PeriodeStoppnivaa(new SokosSimuleringRespons.Stoppnivaa(fagområde, mottaker, tom + " ",
            List.of(beløp).stream().map(b -> new SokosSimuleringRespons.Detaljer(tom.substring(0, 8) + "01 ", tom + " ",
                new BigDecimal(b), "YTEL ")).toList()));
    }

}
