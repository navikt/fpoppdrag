package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.system.os.entiteter.beregningskjema.Beregning;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer;
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class SimuleringResultatTransformer {

    private static final String DATO_PATTERN = "yyyy-MM-dd";

    private TpsTjeneste tpsTjeneste;

    public SimuleringResultatTransformer() {
        // CDI
    }

    @Inject
    public SimuleringResultatTransformer(TpsTjeneste tpsTjeneste) {
        Objects.requireNonNull(tpsTjeneste, "Mangler tpstjeneste");
        this.tpsTjeneste = tpsTjeneste;
    }

    public void mapSimulering(Map<String, SimuleringMottaker.Builder> mottakerBuilderMap, Beregning beregning) {
        String gjelderAktørId = hentAktørIdHvisFnr(beregning.getGjelderId());

        for (BeregningsPeriode periode : beregning.getBeregningsPeriode()) {
            for (BeregningStoppnivaa stoppnivaa : periode.getBeregningStoppnivaa()) {
                String mottakerId = hentAktørIdHvisFnr(stoppnivaa.getUtbetalesTilId());
                SimuleringMottaker.Builder mottakerBuilder;
                if (mottakerBuilderMap.containsKey(mottakerId)) {
                    mottakerBuilder = mottakerBuilderMap.get(mottakerId);
                } else {
                    boolean harSammeAktørIdSomBruker = gjelderAktørId.equals(mottakerId);
                    mottakerBuilder = SimuleringMottaker.builder()
                            .medMottakerNummer(mottakerId)
                            .medMottakerType(utledMottakerType(stoppnivaa.getUtbetalesTilId(), harSammeAktørIdSomBruker));
                    mottakerBuilderMap.put(mottakerId, mottakerBuilder);
                }

                for (BeregningStoppnivaaDetaljer detaljer : stoppnivaa.getBeregningStoppnivaaDetaljer()) {
                    SimulertPostering postering = mapPostering(false, stoppnivaa, detaljer);
                    mottakerBuilder.medSimulertPostering(postering);
                }
            }
        }
    }


    private SimulertPostering mapPostering(boolean utenInntrekk, BeregningStoppnivaa stoppnivaa, BeregningStoppnivaaDetaljer detaljer) {
        SimulertPostering.Builder posteringBuilder = SimulertPostering.builder()
                .medBetalingType(utledBetalingType(detaljer.getBelop()))
                .medBeløp(detaljer.getBelop())
                .medFagOmraadeKode(FagOmrådeKode.fraKodeDefaultUdefinert(stoppnivaa.getKodeFagomraade()))
                .medFom(parseDato(detaljer.getFaktiskFom()))
                .medTom(parseDato(detaljer.getFaktiskTom()))
                .medForfallsdato(parseDato(stoppnivaa.getForfall()))
                .medPosteringType(PosteringType.fraKodeDefaultUdefinert(detaljer.getTypeKlasse()))
                .utenInntrekk(utenInntrekk);

        return posteringBuilder.build();
    }


    public void mapSimuleringUtenInntrekk(Beregning beregning, SimuleringGrunnlag simuleringGrunnlag) {
        SimuleringMottaker mottaker = simuleringGrunnlag.getSimuleringResultat().getSimuleringMottakere()
                .stream()
                .filter(m -> m.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst().orElseThrow(() -> new IllegalStateException("Utviklerfeil: skal ikke komme hit uten at bruker finnes som mottaker"));

        for (BeregningsPeriode periode : beregning.getBeregningsPeriode()) {
            for (BeregningStoppnivaa stoppnivaa : periode.getBeregningStoppnivaa()) {
                for (BeregningStoppnivaaDetaljer detaljer : stoppnivaa.getBeregningStoppnivaaDetaljer()) {
                    SimulertPostering postering = mapPostering(true, stoppnivaa, detaljer);
                    mottaker.leggTilSimulertPostering(postering);
                }
            }
        }
    }


    public String hentAktørIdHvisFnr(String orgNrOrFnr) {
        if (erOrgNr(orgNrOrFnr)) {
            return orgNrOrFnr.substring(2);
        } else {
            AktørId aktørId = tpsTjeneste.hentAktørIdForPersonIdent(new PersonIdent(orgNrOrFnr))
                    .orElseThrow(() -> SimuleringResultatTransformerFeil.FACTORY.fantIkkeAktørIdForFnr().toException());
            return aktørId.getId();
        }
    }

    MottakerType utledMottakerType(String utbetalesTilId, boolean harSammeAktørIdSomBruker) {
        if (harSammeAktørIdSomBruker) {
            return MottakerType.BRUKER;
        }
        if (erOrgNr(utbetalesTilId)) {
            return MottakerType.ARBG_ORG;
        }
        return MottakerType.ARBG_PRIV;
    }

    private boolean erOrgNr(String verdi) {
        Objects.requireNonNull(verdi, "org.nr verdi er null");
        // orgNr i responsen fra økonomi starter med "00"
        return "00".equals(verdi.substring(0, 2));
    }

    BetalingType utledBetalingType(BigDecimal belop) {
        if (belop.compareTo(BigDecimal.ZERO) > 0) {
            return BetalingType.DEBIT;
        }
        return BetalingType.KREDIT;
    }

    private LocalDate parseDato(String dato) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATO_PATTERN);
        return LocalDate.parse(dato, dtf);
    }

    public interface SimuleringResultatTransformerFeil extends DeklarerteFeil {
        SimuleringResultatTransformerFeil FACTORY = FeilFactory.create(SimuleringResultatTransformerFeil.class);

        @TekniskFeil(feilkode = "FPO-952153", feilmelding = "Fant ikke aktørId for FNR", logLevel = LogLevel.WARN)
        Feil fantIkkeAktørIdForFnr();
    }
}
