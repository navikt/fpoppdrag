package no.nav.foreldrepenger.oppdrag.iverksett;

import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.BEHANDLING_ID;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.SAKSNUMMER;
import static no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTestDataHelper.opprettTilkjentYtelseV1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.TilkjentYtelse;
import no.nav.foreldrepenger.oppdrag.dbstoette.FPoppdragEntityManagerAwareExtension;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseRepository;
import no.nav.vedtak.log.util.MemoryAppender;

@ExtendWith(FPoppdragEntityManagerAwareExtension.class)
public class TilkjentYtelseTjenesteTest {

    public final MemoryAppender logSniffer = MemoryAppender.sniff(TilkjentYtelseTjeneste.class);

    private TilkjentYtelseRepository tilkjentYtelseRepository;
    private final TilkjentYtelseRestKlient restClient = mock(TilkjentYtelseRestKlient.class);
    private TilkjentYtelseTjeneste tilkjentYtelseTjeneste;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        tilkjentYtelseRepository = new TilkjentYtelseRepository(entityManager);
        tilkjentYtelseTjeneste = new TilkjentYtelseTjeneste(tilkjentYtelseRepository, restClient);
    }

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
        tilkjentYtelseTjeneste.hentOgLagreTilkjentYtelse(BEHANDLING_ID);

        //Assert
        assertThat(logSniffer.countEntries("Har allerede hentet og lagret tilkjent ytelse for behandlingId=100000123. Ignorerer.")).isEqualTo(1);
        //Blir kalt en gang i første hentOgLagreTilkjentYtelse
        verify(restClient).hentTilkjentYtelse(BEHANDLING_ID);
    }
}
