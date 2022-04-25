package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

public class SimuleringResultatTransformerTest {

    PersonTjeneste tpsTjeneste = mock(PersonTjeneste.class);
    SimuleringResultatTransformer simuleringResultatTransformer = new SimuleringResultatTransformer(tpsTjeneste);

    @Test
    public void utlederMottakerTypeBruker() {
        String mottakerId = "30048949955";

        MottakerType mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, true);
        assertThat(mottakerType).isEqualTo(MottakerType.BRUKER);
    }

    @Test
    public void utlederMottakerTypeARBG_ORG() {
        String mottakerId = "00984528749";

        MottakerType mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, false);
        assertThat(mottakerType).isEqualTo(MottakerType.ARBG_ORG);
    }

    @Test
    public void utlederMottakerTypeARBG_PRIV() {
        String mottakerId = "26047249944";

        MottakerType mottakerType = simuleringResultatTransformer.utledMottakerType(mottakerId, false);
        assertThat(mottakerType).isEqualTo(MottakerType.ARBG_PRIV);
    }

    @Test
    public void utlederBetalingTypeDebit() {
        BetalingType betalingTypeBeløp = simuleringResultatTransformer.utledBetalingType(BigDecimal.valueOf(1));
        assertThat(betalingTypeBeløp).isEqualTo(BetalingType.D);
    }


    @Test
    public void utlederBetalingTypeKredit() {
        BetalingType betalingTypeBeløp = simuleringResultatTransformer.utledBetalingType(BigDecimal.valueOf(-1));
        assertThat(betalingTypeBeløp).isEqualTo(BetalingType.K);
    }

    @Test
    public void returnererStrippetOrgnrHvisIkkeFnr() {
        String mottattOrgnr = "00984528749";
        String strippetOrgnr = "984528749";

        String orgnr = simuleringResultatTransformer.hentAktørIdHvisFnr(mottattOrgnr);
        assertThat(orgnr).isEqualTo(strippetOrgnr);
    }

    @Test
    public void returnererAktørIdHvisFnr() {
        String fnr = "30048949955";
        AktørId aktørId = new AktørId("12345");

        when(tpsTjeneste.hentAktørForFnr(Mockito.eq(new PersonIdent(fnr)))).thenReturn(Optional.of(aktørId));

        String resultat = simuleringResultatTransformer.hentAktørIdHvisFnr(fnr);
        assertThat(resultat).isEqualTo(aktørId.getId());
    }


}
