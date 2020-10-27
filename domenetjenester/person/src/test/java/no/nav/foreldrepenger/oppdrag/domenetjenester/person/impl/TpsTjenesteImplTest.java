package no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;

public class TpsTjenesteImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private AktørConsumerMedCache aktørConsumer = Mockito.mock(AktørConsumerMedCache.class);

    private TpsTjeneste tpsTjeneste = new TpsTjeneste(aktørConsumer, mock(PdlKlient.class));

    @Test
    public void finnerAktørIdForFnr() {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";

        when(aktørConsumer.hentAktørIdForPersonIdent(eq(fnr))).thenReturn(Optional.of(aktørId.getId()));

        // Act
        Optional<AktørId> funnetAktørId = tpsTjeneste.hentAktørIdForPersonIdent(PersonIdent.fra(fnr));

        // Assert
        assertThat(funnetAktørId).isPresent();
        assertThat(funnetAktørId.get()).isEqualTo(aktørId);
    }

    @Test
    public void finnerFnrForAktørId() {
        // Arrange
        AktørId aktørId = new AktørId("12345");
        String fnr = "24069305608";
        when(aktørConsumer.hentPersonIdentForAktørId(eq(aktørId.getId()))).thenReturn(Optional.of(fnr));

        // Act
        Optional<PersonIdent> funnetPersonIdent = tpsTjeneste.hentIdentForAktørId(aktørId);

        // Assert
        assertThat(funnetPersonIdent).isPresent();
        assertThat(funnetPersonIdent.get().getIdent()).isEqualTo(fnr);
    }


}