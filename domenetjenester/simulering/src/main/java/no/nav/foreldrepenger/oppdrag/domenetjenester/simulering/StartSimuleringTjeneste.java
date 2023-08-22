package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.FpWsProxySimuleringKlient;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class StartSimuleringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(StartSimuleringTjeneste.class);

    private static final String DEAKTIVER_SIMULERING_DEAKTIVERING = "testing.deaktiver.simulering.deaktivering";

    private SimuleringRepository simuleringRepository;
    private FpWsProxySimuleringKlient fpWsProxySimuleringKlient;
    private SimuleringResultatTransformer resultatTransformer;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    StartSimuleringTjeneste() {
        // CDI
    }

    @Inject
    public StartSimuleringTjeneste(SimuleringRepository simuleringRepository,
                                   FpWsProxySimuleringKlient fpWsProxySimuleringKlient,
                                   SimuleringResultatTransformer resultatTransformer,
                                   SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.fpWsProxySimuleringKlient = fpWsProxySimuleringKlient;
        this.resultatTransformer = resultatTransformer;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
    }

    public void startSimulering(OppdragskontrollDto oppdragskontrollDto) {
        long t0 = System.currentTimeMillis();
        var behandlingId = oppdragskontrollDto.behandlingId();
        LOG.info("Starter simulering. behandlingID={} oppdragantall={}", behandlingId, oppdragskontrollDto.oppdrag().size());

        var simuleringResponsListe = utførSimuleringViaFpWsProxy(oppdragskontrollDto);

        LOG.info("Simulering svarmeldinger mottatt. behandlingID={} tidsforbruk={} ms",
                behandlingId,
                System.currentTimeMillis() - t0);

        if (harIkkeTomRespons(simuleringResponsListe)) {
            var ytelseType = bestemYtelseType(behandlingId, oppdragskontrollDto.oppdrag());
            var simuleringGrunnlag = transformerTilDatastruktur(behandlingId, simuleringResponsListe, ytelseType);

            utførSimuleringUtenInntrekk(oppdragskontrollDto, simuleringGrunnlag);

            simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        } else {
            // Deaktiverer forrige simuleringgrunnlag hvis den nye simuleringen returnerer et tomt svar
            // f.eks. oppdrag finnes fra før, tom respons fra øk, FeilUnderBehandling som ikke er error
            // for å unngå å sende simuleringsresultat for forrige simulering når den nye simuleringen returnerer tomt resultat
            deaktiverBehandling(behandlingId);

        }
        LOG.info("Fullført simulering. behandlingID={} tidsforbrukTotalt={} ms", behandlingId, System.currentTimeMillis() - t0);
    }

    private List<BeregningDto> utførSimuleringViaFpWsProxy(OppdragskontrollDto oppdragskontrollDto) {
        return utførSimuleringViaFpWsProxy(oppdragskontrollDto, null, false);
    }

    private List<BeregningDto> utførSimuleringViaFpWsProxy(OppdragskontrollDto oppdragskontrollDto, YtelseType ytelseType, boolean utenInntrekk) {
        return fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(oppdragskontrollDto, ytelseType, utenInntrekk);
    }

    private static boolean harIkkeTomRespons(List<BeregningDto> beregningDtoListe) {
        return beregningDtoListe != null && beregningDtoListe.stream()
                .filter(Objects::nonNull)
                .map(BeregningDto::gjelderId)
                .anyMatch(Objects::nonNull);
    }

    private void utførSimuleringUtenInntrekk(OppdragskontrollDto oppdragskontrollDto, SimuleringGrunnlag simuleringGrunnlag) {
        var beregningResultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);
        if (erResultatMedInntrekkOgFeilutbetaling(beregningResultat)) {
            LOG.info("Utfører simulering uten inntrekk.");
            var beregningResultater = utførSimuleringViaFpWsProxy(oppdragskontrollDto, simuleringGrunnlag.getYtelseType(), true);
            beregningResultater.stream()
                    .filter(Objects::nonNull)
                    .forEach(beregningDto -> resultatTransformer.mapSimuleringUtenInntrekk(beregningDto, simuleringGrunnlag));
        }
    }

    private boolean erResultatMedInntrekkOgFeilutbetaling(BeregningResultat beregningResultat) {
        var inntrekkNesteUtbetaling = beregningResultat.getOppsummering().getInntrekkNesteUtbetaling();
        var feilutbetaling = beregningResultat.getOppsummering().getFeilutbetaling();
        return inntrekkNesteUtbetaling != null && inntrekkNesteUtbetaling.compareTo(BigDecimal.ZERO) != 0
                && feilutbetaling != null && feilutbetaling.compareTo(BigDecimal.ZERO) != 0;
    }

    private YtelseType bestemYtelseType(Long behandlingId, List<Oppdrag110Dto> oppdrag) {
        var fagOmrådeKoder = oppdrag.stream()
                .map(Oppdrag110Dto::kodeFagomrade)
                .distinct()
                .toList();
        if (fagOmrådeKoder.isEmpty()) {
            LOG.warn("Fant ingen fagområdeKoder for behandlingId={}", behandlingId);
            throw new IllegalStateException(String.format("Utvikler-feil: Ytelse Type må være satt for behandling: %s", behandlingId));
        }
        var ytelsetyper = fagOmrådeKoder.stream()
                .map(Enum::name)
                .map(Fagområde::fraKode)
                .map(YtelseUtleder::utledFor)
                .distinct()
                .toList();

        if (ytelsetyper.size() > 1) {
            LOG.warn("Ikke mulig å simulere for flere ytelser sammtidig for behandligId={}", behandlingId);
            throw new TekniskException("FPO-810466", String.format("Utvikler-feil: Klarer ikke utlede unik ytelsetype for behandlingId=%s fagområdekode=%s", behandlingId, fagOmrådeKoder));
        }

        var ytelseType = ytelsetyper.stream().findFirst();
        if (ytelseType.isEmpty()) {
            throw new TekniskException("FPO-852146", String.format("Utvikler-feil: Mangler mapping mellom fagområdekode og ytelsetype for behandlingId=%s fagområdekode=%s", behandlingId, fagOmrådeKoder));
        }
        return ytelseType.get();
    }

    private SimuleringGrunnlag transformerTilDatastruktur(long behandlingId, List<BeregningDto> beregningDtoListe, YtelseType ytelseType) {
        // Finn første gjelderId hvor responsen ikke er en null-verdi
        var gjelderIdFnr = beregningDtoListe.stream()
                .filter(Objects::nonNull)
                .map(BeregningDto::gjelderId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Utvikler-feil: skulle ikke kommet hit med bare null-responser"));

        var gjelderId = resultatTransformer.hentAktørIdHvisFnr(gjelderIdFnr);

        var simuleringResultatBuilder = SimuleringResultat.builder();

        Map<String, SimuleringMottaker.Builder> mottakerBuilderMap = new HashMap<>();
        for (var beregningDto : beregningDtoListe) {
            if (beregningDto == null) {
                continue;
            }
            resultatTransformer.mapSimulering(mottakerBuilderMap, beregningDto);

        }
        mottakerBuilderMap.forEach((key, builder) -> simuleringResultatBuilder.medSimuleringMottaker(builder.build()));

        return SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(gjelderId)
                .medSimuleringResultat(simuleringResultatBuilder.build())
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medYtelseType(ytelseType)
                .build();
    }

    public void kansellerSimulering(Long behandlingId) {
        deaktiverBehandling(behandlingId);
    }

    private void deaktiverBehandling(long behandlingId) {
        var deaktiverForLokalTesting = Environment.current().getProperty(DEAKTIVER_SIMULERING_DEAKTIVERING);
        if (deaktiverForLokalTesting == null || "false".equalsIgnoreCase(deaktiverForLokalTesting)) {
            var eksisterende = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
            eksisterende.ifPresent(grunnlag -> {
                LOG.info("Deaktiverer simulering for behandling {}", behandlingId);
                simuleringRepository.deaktiverSimuleringGrunnlag(grunnlag);
            });
        }
    }
}
