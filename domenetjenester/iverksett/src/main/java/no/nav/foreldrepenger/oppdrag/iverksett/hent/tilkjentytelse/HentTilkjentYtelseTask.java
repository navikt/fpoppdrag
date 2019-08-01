package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.oppdrag.iverksett.TilkjentYtelseTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(HentTilkjentYtelseTask.TASKTYPE)
public class HentTilkjentYtelseTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(HentTilkjentYtelseTask.class);

    static final String TASKTYPE = "iverksetteØkonomi.hentTilkjentYtelse";

    private TilkjentYtelseTjeneste tilkjentYtelseTjeneste;

    HentTilkjentYtelseTask() {
        //for CDI proxy
    }

    @Inject
    public HentTilkjentYtelseTask(TilkjentYtelseTjeneste tilkjentYtelseTjeneste) {
        this.tilkjentYtelseTjeneste = tilkjentYtelseTjeneste;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        logger.info("Kjører for behandlingId={}", behandlingId);
        tilkjentYtelseTjeneste.hentOgLagreTilkjentYtelse(behandlingId);
    }
}
