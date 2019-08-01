package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.feed.poller.PostTransactionHandler;
import no.nav.foreldrepenger.oppdrag.feed.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class TilkjentYtelseReader {

    private static final Logger logger = LoggerFactory.getLogger(TilkjentYtelseReader.class);

    private TilkjentYtelseMeldingConsumer meldingConsumer;
    private ProsessTaskRepository prosessTaskRepository;

    TilkjentYtelseReader() {
        // for CDI proxy
    }

    @Inject
    public TilkjentYtelseReader(TilkjentYtelseMeldingConsumer meldingConsumer, ProsessTaskRepository prosessTaskRepository) {
        this.meldingConsumer = meldingConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public PostTransactionHandler hentOgBehandleMeldinger() {

        List<TilkjentYtelseMelding> meldinger = meldingConsumer.lesMeldinger();
        if (meldinger.isEmpty()) {
            return () -> {
            }; //trenger ikke å gjøre commit, siden ingen nye meldinger er lest
        }

        logger.info("Leste {} meldinger fra fp-tilkjentytelse-v1-topic", meldinger.size());
        behandleMeldinger(meldinger);
        return this::commitMeldinger;
    }

    private void behandleMeldinger(List<TilkjentYtelseMelding> meldinger) {
        for (TilkjentYtelseMelding melding : meldinger) {
            prosesserMelding(melding);
        }
    }

    public void commitMeldinger() {
        try {
            meldingConsumer.manualCommitSync();
        } catch (CommitFailedException e) {
            throw KafkaConsumerFeil.FACTORY.kunneIkkeCommitOffset(e.getClass(), e.getMessage(), e).toException();
        }
    }

    private void prosesserMelding(TilkjentYtelseMelding melding) {
        opprettProsessTask(melding);
    }

    private void opprettProsessTask(TilkjentYtelseMelding melding) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(HentTilkjentYtelseTask.TASKTYPE);
        prosessTaskData.setBehandling(melding.getFagsakId(), melding.getBehandlingId(), melding.getAktørId());
        prosessTaskData.setProperty("iverksettingSystem", melding.getIverksettingSystem());
        prosessTaskRepository.lagre(prosessTaskData);

        logger.info("Opprettet prosesstask {} for behandlingId={}", HentTilkjentYtelseTask.TASKTYPE, melding.getBehandlingId());
    }


}
