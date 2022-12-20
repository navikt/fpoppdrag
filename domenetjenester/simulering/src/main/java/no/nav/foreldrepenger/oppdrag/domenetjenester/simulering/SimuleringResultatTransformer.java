package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDetaljerDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class SimuleringResultatTransformer {

    private static final String DATO_PATTERN = "yyyy-MM-dd";

    private PersonTjeneste personTjeneste;

    public SimuleringResultatTransformer() {
        // CDI
    }

    @Inject
    public SimuleringResultatTransformer(PersonTjeneste personTjeneste) {
        Objects.requireNonNull(personTjeneste, "Mangler tpstjeneste");
        this.personTjeneste = personTjeneste;
    }

    public void mapSimulering(Map<String, SimuleringMottaker.Builder> mottakerBuilderMap, BeregningDto beregning) {
        var gjelderAktørId = hentAktørIdHvisFnr(beregning.gjelderId());

        for (var periode : beregning.beregningsPeriode()) {
            for (var stoppnivaa : periode.beregningStoppnivaa()) {
                var mottakerId = hentAktørIdHvisFnr(stoppnivaa.utbetalesTilId());
                SimuleringMottaker.Builder mottakerBuilder;
                if (mottakerBuilderMap.containsKey(mottakerId)) {
                    mottakerBuilder = mottakerBuilderMap.get(mottakerId);
                } else {
                    var harSammeAktørIdSomBruker = gjelderAktørId.equals(mottakerId);
                    mottakerBuilder = SimuleringMottaker.builder()
                            .medMottakerNummer(mottakerId)
                            .medMottakerType(utledMottakerType(stoppnivaa.utbetalesTilId(), harSammeAktørIdSomBruker));
                    mottakerBuilderMap.put(mottakerId, mottakerBuilder);
                }

                for (var detaljer : stoppnivaa.beregningStoppnivaaDetaljer()) {
                    SimulertPostering postering = mapPostering(false, stoppnivaa, detaljer);
                    mottakerBuilder.medSimulertPostering(postering);
                }
            }
        }
    }


    private SimulertPostering mapPostering(boolean utenInntrekk, BeregningStoppnivåDto stoppnivaa, BeregningStoppnivåDetaljerDto detaljer) {
        return SimulertPostering.builder()
                .medBetalingType(utledBetalingType(detaljer.belop()))
                .medBeløp(detaljer.belop())
                .medFagOmraadeKode(Fagområde.fraKode(stoppnivaa.kodeFagomraade()))
                .medFom(parseDato(detaljer.faktiskFom()))
                .medTom(parseDato(detaljer.faktiskTom()))
                .medForfallsdato(parseDato(stoppnivaa.forfall()))
                .medPosteringType(PosteringType.getOrNull(detaljer.typeKlasse()))
                .utenInntrekk(utenInntrekk)
                .build();
    }


    public void mapSimuleringUtenInntrekk(BeregningDto beregning, SimuleringGrunnlag simuleringGrunnlag) {
        var mottaker = simuleringGrunnlag.getSimuleringResultat().getSimuleringMottakere().stream()
                .filter(m -> m.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: skal ikke komme hit uten at bruker finnes som mottaker"));

        for (var periode : beregning.beregningsPeriode()) {
            for (var stoppnivaa : periode.beregningStoppnivaa()) {
                for (var detaljer : stoppnivaa.beregningStoppnivaaDetaljer()) {
                    var postering = mapPostering(true, stoppnivaa, detaljer);
                    mottaker.leggTilSimulertPostering(postering);
                }
            }
        }
    }


    public String hentAktørIdHvisFnr(String orgNrOrFnr) {
        if (erOrgNr(orgNrOrFnr)) {
            return orgNrOrFnr.substring(2);
        } else {
            var aktørId = personTjeneste.hentAktørForFnr(new PersonIdent(orgNrOrFnr))
                    .orElseThrow(() -> new TekniskException("FPO-952153", "Fant ikke aktørId for FNR"));
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
            return BetalingType.D;
        }
        return BetalingType.K;
    }

    private static LocalDate parseDato(String dato) {
        return LocalDate.parse(dato, DateTimeFormatter.ofPattern(DATO_PATTERN));
    }
}
