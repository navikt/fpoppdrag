package no.nav.foreldrepenger.oppdrag.web.app.selftest;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.web.app.selftest.checks.DatabaseHealthCheck;
import no.nav.foreldrepenger.konfig.KonfigVerdi;

@ApplicationScoped
public class Selftests {

    private DatabaseHealthCheck databaseHealthCheck;

    private boolean isDatabaseReady;
    private LocalDateTime sistOppdatertTid = LocalDateTime.now().minusDays(1);

    private String applicationName;
    private SelftestResultat selftestResultat;




    @Inject
    public Selftests(
            DatabaseHealthCheck databaseHealthCheck,
            @KonfigVerdi(value = "application.name") String applicationName) {
        this.databaseHealthCheck = databaseHealthCheck;
        this.applicationName = applicationName;
    }

    Selftests() {
        // for CDI proxy
    }

    public boolean isReady() {
        oppdaterSelftestResultatHvisNødvendig();
        return !SelftestResultat.AggregateResult.ERROR.equals(selftestResultat.getAggregateResult());
    }

    public boolean isAlive() {
        // Bruk denne for NAIS-respons og skill omfanget her.
        oppdaterSelftestResultatHvisNødvendig();
        return isDatabaseReady; // NOSONAR
    }

    private synchronized void oppdaterSelftestResultatHvisNødvendig() {
        if (sistOppdatertTid.isBefore(LocalDateTime.now().minusSeconds(30))) {
            isDatabaseReady = databaseHealthCheck.isOK();
            selftestResultat = innhentSelftestResultat();
            sistOppdatertTid = LocalDateTime.now();
        }
    }

    private SelftestResultat innhentSelftestResultat() {
        SelftestResultat samletResultat = new SelftestResultat();
        samletResultat.setApplication(applicationName);
        samletResultat.setTimestamp(LocalDateTime.now());

        samletResultat.leggTilResultatForKritiskTjeneste(isDatabaseReady, databaseHealthCheck.getDescription(), databaseHealthCheck.getEndpoint());

        return samletResultat;
    }

}
