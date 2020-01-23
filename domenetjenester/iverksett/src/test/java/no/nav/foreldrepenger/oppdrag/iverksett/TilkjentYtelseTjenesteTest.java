package no.nav.foreldrepenger.oppdrag.iverksett;

import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.BEHANDLING_ID;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.SAKSNUMMER;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.opprettTilkjentYtelseV1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.TilkjentYtelse;
import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseRepository;
import no.nav.foreldrepenger.oppdrag.test.LogSniffer;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class TilkjentYtelseTjenesteTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private Repository repository = repositoryRule.getRepository();

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    private TilkjentYtelseRepository tilkjentYtelseRepository = new TilkjentYtelseRepository(repositoryRule.getEntityManager());
    private TilkjentYtelseRestKlient restClient = mock(TilkjentYtelseRestKlient.class);
    private TilkjentYtelseTjeneste tilkjentYtelseTjeneste = new TilkjentYtelseTjeneste(tilkjentYtelseRepository, restClient);

    @Test
    public void skal_hente_tilkjent_ytelse_fra_fpsak_og_lagre_i_fpoppdrag() {
        //Arrange
        TilkjentYtelse tilkjentYtelse = opprettTilkjentYtelseV1();
        when(restClient.hentTilkjentYtelse(BEHANDLING_ID)).thenReturn(tilkjentYtelse);

        //Act
        tilkjentYtelseTjeneste.hentOgLagreTilkjentYtelse(BEHANDLING_ID);

        //Assert
        verify(restClient).hentTilkjentYtelse(BEHANDLING_ID);
        Optional<TilkjentYtelseEntitet> tilkjentYtelseEntitetOpt = tilkjentYtelseRepository.hentTilkjentYtelse(BEHANDLING_ID);
        assertThat(tilkjentYtelseEntitetOpt).isPresent();
        assertThat(tilkjentYtelseEntitetOpt.get().getTilkjentYtelseBehandlingInfo().getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    public void skal_ikke_hente_tilkjent_ytelse_fra_fpsak_hvis_det_allerede_finnes_for_behandlingen() {
        //Arrange
        TilkjentYtelse tilkjentYtelse = opprettTilkjentYtelseV1();
        when(restClient.hentTilkjentYtelse(BEHANDLING_ID)).thenReturn(tilkjentYtelse);

        //Act
        tilkjentYtelseTjeneste.hentOgLagreTilkjentYtelse(BEHANDLING_ID);
        repository.flushAndClear();
        tilkjentYtelseTjeneste.hentOgLagreTilkjentYtelse(BEHANDLING_ID);

        //Assert
        logSniffer.assertHasInfoMessage("Har allerede hentet og lagret tilkjent ytelse for behandlingId=100000123. Ignorerer.");
        //Blir kalt en gang i første hentOgLagreTilkjentYtelse
        verify(restClient).hentTilkjentYtelse(BEHANDLING_ID);
    }
}