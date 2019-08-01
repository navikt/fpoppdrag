package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HentTilkjentYtelseTaskTest {

    private static final Long BEHANDLING_ID = 1L;

    private ProsessTaskData prosessTaskData = mock(ProsessTaskData.class);
    private TilkjentYtelseTjeneste tilkjentYtelseTjeneste = mock(TilkjentYtelseTjeneste.class);

    private HentTilkjentYtelseTask hentTilkjentYtelseTask;

    @Before
    public void setUp() {
        when(prosessTaskData.getBehandlingId()).thenReturn(BEHANDLING_ID);
        hentTilkjentYtelseTask = new HentTilkjentYtelseTask(tilkjentYtelseTjeneste);
    }

    @Test
    public void task_skal_hente_tilkjent_ytelse() {
        //Act
        hentTilkjentYtelseTask.doTask(prosessTaskData);

        //Assert
        verify(tilkjentYtelseTjeneste).hentOgLagreTilkjentYtelse(BEHANDLING_ID);
    }
}