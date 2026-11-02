package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.FpWsProxySimuleringKlient;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosOppdragProxyKlient;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosSimuleringRespons;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class StartSimuleringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(StartSimuleringTjeneste.class);

    private boolean skalKalleSokos;

    private SimuleringRepository simuleringRepository;
    private FpWsProxySimuleringKlient fpWsProxySimuleringKlient;
    private SokosOppdragProxyKlient sokosOppdragProxyKlient;
    private SimuleringResultatTransformer resultatTransformer;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    StartSimuleringTjeneste() {
        // CDI
    }

    @Inject
    public StartSimuleringTjeneste(SimuleringRepository simuleringRepository,
                                   FpWsProxySimuleringKlient fpWsProxySimuleringKlient,
                                   SokosOppdragProxyKlient sokosOppdragProxyKlient,
                                   SimuleringResultatTransformer resultatTransformer,
                                   SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this(simuleringRepository, fpWsProxySimuleringKlient, sokosOppdragProxyKlient, resultatTransformer,
            simuleringBeregningTjeneste, Environment.current().getProperty("sokos.simulering.sammenlign", Boolean.class, false));
    }

    StartSimuleringTjeneste(SimuleringRepository simuleringRepository,
                            FpWsProxySimuleringKlient fpWsProxySimuleringKlient,
                            SokosOppdragProxyKlient sokosOppdragProxyKlient,
                            SimuleringResultatTransformer resultatTransformer,
                            SimuleringBeregningTjeneste simuleringBeregningTjeneste,
                            boolean skalKalleSokos) {
        this.simuleringRepository = simuleringRepository;
        this.fpWsProxySimuleringKlient = fpWsProxySimuleringKlient;
        this.sokosOppdragProxyKlient = sokosOppdragProxyKlient;
        this.resultatTransformer = resultatTransformer;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
        this.skalKalleSokos = skalKalleSokos;
    }

    public void startSimulering(OppdragskontrollDto oppdragskontrollDto) {
        long t0 = System.currentTimeMillis();
        var behandlingId = Long.parseLong(oppdragskontrollDto.behandlingId());
        LOG.info("Starter simulering. behandlingID={} oppdragantall={}", behandlingId, oppdragskontrollDto.oppdrag().size());

        var simuleringResponsListe = utførSimuleringViaFpWsProxy(oppdragskontrollDto);

        LOG.info("Simulering svarmeldinger mottatt. behandlingID={} tidsforbruk={} ms",
                behandlingId,
                System.currentTimeMillis() - t0);

        if (harIkkeTomRespons(simuleringResponsListe)) {
            var ytelseType = bestemYtelseType(behandlingId, oppdragskontrollDto.oppdrag());
            var simuleringGrunnlag = transformerTilDatastruktur(behandlingId, simuleringResponsListe, ytelseType);

            var simulertUtenInntrekk = utførSimuleringUtenInntrekk(oppdragskontrollDto, simuleringGrunnlag);

            if (skalKalleSokos) {
                sammenlignMedSokos(oppdragskontrollDto, simuleringGrunnlag, simulertUtenInntrekk);
            }

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

    private boolean utførSimuleringUtenInntrekk(OppdragskontrollDto oppdragskontrollDto, SimuleringGrunnlag simuleringGrunnlag) {
        var beregningResultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);
        if (erResultatMedInntrekkOgFeilutbetaling(beregningResultat)) {
            LOG.info("Utfører simulering uten inntrekk.");
            var beregningResultater = utførSimuleringViaFpWsProxy(oppdragskontrollDto, simuleringGrunnlag.getYtelseType(), true);
            beregningResultater.stream()
                    .filter(Objects::nonNull)
                    .forEach(beregningDto -> resultatTransformer.mapSimuleringUtenInntrekk(beregningDto, simuleringGrunnlag));
            return true;
        }
        return false;
    }

    private void sammenlignMedSokos(OppdragskontrollDto oppdrag, SimuleringGrunnlag fasit, boolean simulertUtenInntrekk) {
        try {
            var beregninger = hentSokosBeregninger(oppdrag, fasit.getYtelseType(), false);
            if (beregninger.isEmpty()) {
                LOG.info("Sokos-sammenligning: tomt resultat fra Sokos, grunnlag fra FpWsProxy. behandlingId={}", oppdrag.behandlingId());
                return;
            }
            var sokosGrunnlag = resultatTransformer.mapSokosSimulering(fasit.getEksternReferanse().getBehandlingId(),
                beregninger, fasit.getYtelseType());
            if (simulertUtenInntrekk) {
                hentSokosBeregninger(oppdrag, fasit.getYtelseType(), true)
                    .forEach(beregning -> resultatTransformer.mapSokosSimuleringUtenInntrekk(beregning, sokosGrunnlag));
            }
            if (!fasit.equals(sokosGrunnlag)) {
                loggForskjeller(oppdrag.behandlingId(), fasit, sokosGrunnlag);
            } else {
                LOG.info("Sokos-sammenligning: like grunnlag. behandlingId={} utenInntrekk={}", oppdrag.behandlingId(), simulertUtenInntrekk);
            }
        } catch (Exception e) {
            // Shadow-kallet skal aldri endre responsen eller nedetidsprotokollen til fp-sak.
            LOG.info("Sokos-sammenligning feilet. behandlingId={} feiltype={}", oppdrag.behandlingId(), e.getClass().getSimpleName());
        }
    }

    private List<SokosSimuleringRespons.Beregning> hentSokosBeregninger(OppdragskontrollDto oppdrag, YtelseType ytelseType, boolean utenInntrekk) {
        return sokosOppdragProxyKlient.utførSimulering(oppdrag, ytelseType, utenInntrekk).stream()
            .map(SokosSimuleringRespons.Respons::beregning)
            .filter(Objects::nonNull)
            .filter(beregning -> beregning.gjelderId() != null && !beregning.gjelderId().isBlank())
            .toList();
    }

    private static void loggForskjeller(String behandlingId, SimuleringGrunnlag fasit, SimuleringGrunnlag sokos) {
        var forventede = fasit.getSimuleringResultat().getSimuleringMottakere();
        var faktiske = new ArrayList<>(sokos.getSimuleringResultat().getSimuleringMottakere());
        LOG.info("Sokos-sammenligning: ulike grunnlag. behandlingId={} mottakereFpWsProxy={} mottakereSokos={}",
            behandlingId, forventede.size(), faktiske.size());
        for (var forventet : forventede) {
            var faktisk = faktiske.stream()
                .filter(m -> m.getMottakerNummer().equals(forventet.getMottakerNummer()) && m.getMottakerType() == forventet.getMottakerType())
                .findFirst();
            if (faktisk.isEmpty()) {
                LOG.info("Sokos-sammenligning: mangler mottaker. behandlingId={} mottakerType={}", behandlingId, forventet.getMottakerType());
            } else {
                faktiske.remove(faktisk.get());
                loggPosteringsavvik(behandlingId, forventet, faktisk.get(), false);
                loggPosteringsavvik(behandlingId, forventet, faktisk.get(), true);
            }
        }
        faktiske.forEach(m -> LOG.info("Sokos-sammenligning: ekstra mottaker. behandlingId={} mottakerType={}", behandlingId, m.getMottakerType()));
    }

    private static void loggPosteringsavvik(String behandlingId, SimuleringMottaker fasit, SimuleringMottaker sokos, boolean utenInntrekk) {
        var forventede = utenInntrekk ? fasit.getSimulertePosteringerUtenInntrekk() : fasit.getSimulertePosteringer();
        var ekstra = new ArrayList<>(utenInntrekk ? sokos.getSimulertePosteringerUtenInntrekk() : sokos.getSimulertePosteringer());
        List<SimulertPostering> manglende = forventede.stream().filter(p -> !ekstra.remove(p)).toList();
        if (!manglende.isEmpty() || !ekstra.isEmpty()) {
            LOG.info("Sokos-sammenligning: ulike posteringer. behandlingId={} mottakerType={} utenInntrekk={} manglerISokos={} ekstraISokos={}",
                behandlingId, fasit.getMottakerType(), utenInntrekk, manglende.size(), ekstra.size());
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

    private SimuleringGrunnlag transformerTilDatastruktur(Long behandlingId, List<BeregningDto> beregningDtoListe, YtelseType ytelseType) {
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
        var eksisterende = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        eksisterende.ifPresent(grunnlag -> {
            LOG.info("Deaktiverer simulering for behandling {}", behandlingId);
            simuleringRepository.deaktiverSimuleringGrunnlag(grunnlag);
        });
    }
}
