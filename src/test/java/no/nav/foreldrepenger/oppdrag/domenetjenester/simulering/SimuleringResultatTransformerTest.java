package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
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


}
