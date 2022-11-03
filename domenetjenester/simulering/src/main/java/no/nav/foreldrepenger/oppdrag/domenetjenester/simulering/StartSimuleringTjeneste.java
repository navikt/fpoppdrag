package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragSkjemaConstants;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.oppdrag.OppdragConsumer;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.OppdragMapper;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.SimuleringResultatTransformer;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.xmlutils.JaxbHelper;
import no.nav.system.os.entiteter.beregningskjema.Beregning;
import no.nav.system.os.entiteter.oppdragskjema.Ompostering;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.ObjectFactory;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class StartSimuleringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(StartSimuleringTjeneste.class);
    private static final Logger SECURE_LOGGER = LoggerFactory.getLogger("secureLogger");

    private static final String DEAKTIVER_SIMULERING_DEAKTIVERING = "testing.deaktiver.simulering.deaktivering";
    protected static final String KODE_ENDRING = "ENDR";

    private SimuleringRepository simuleringRepository;
    private OppdragConsumer oppdragConsumer;
    private SimuleringResultatTransformer resultatTransformer;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    StartSimuleringTjeneste() {
        // CDI
    }

    @Inject
    public StartSimuleringTjeneste(SimuleringRepository simuleringRepository,
                                   OppdragConsumer oppdragConsumer,
                                   SimuleringResultatTransformer resultatTransformer, SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.oppdragConsumer = oppdragConsumer;
        this.resultatTransformer = resultatTransformer;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
    }

    private static boolean harIkkeTomRespons(List<SimulerBeregningResponse> simuleringResponsListe) {
        return simuleringResponsListe != null && simuleringResponsListe.stream()
                .filter(Objects::nonNull)
                .map(SimulerBeregningResponse::getResponse)
                .filter(Objects::nonNull)
                .map(no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse::getSimulering)
                .filter(Objects::nonNull)
                .map(Beregning::getGjelderId)
                .anyMatch(Objects::nonNull);
    }

    private static String størrelse(List<String> oppdragXmlListe) {
        long sum = 0;
        for (String s : oppdragXmlListe) {
            sum += s.length();
        }

        if (sum >= 1000) {
            //output i Kilobyte for enkler å lese, avrundet til hele kB
            return (sum + 500) / 1000 + " kB";
        } else {
            //output i bytes for å ha nøyaktighet når nær 0
            return sum + " B";
        }

    }

    public void startSimulering(Long behandlingId, List<String> oppdragXmlListe) {
        long t0 = System.currentTimeMillis();
        String totalStørrelseUt = størrelse(oppdragXmlListe);
        LOG.info("Starter simulering. behandlingID={} oppdragantall={} totalstørrelseUt={}",
                behandlingId,
                oppdragXmlListe.size(),
                totalStørrelseUt);

        List<Oppdrag> simuleringOppdragListe = konverterOppdragXmlTilSimuleringOppdrag(oppdragXmlListe);
        List<SimulerBeregningRequest> simuleringRequestListe = opprettBeregningRequestListe(simuleringOppdragListe);

        List<SimulerBeregningResponse> simuleringResponsListe = utførSimulering(simuleringRequestListe);

        LOG.info("Simulering svarmeldinger mottatt. behandlingID={} oppdragantall={} totalstørrelseUt={} tidsforbruk={} ms",
                behandlingId,
                oppdragXmlListe.size(),
                totalStørrelseUt,
                System.currentTimeMillis() - t0);

        if (harIkkeTomRespons(simuleringResponsListe)) {
            YtelseType ytelseType = bestemYtelseType(behandlingId, simuleringOppdragListe);
            SimuleringGrunnlag simuleringGrunnlag = transformerTilDatastruktur(behandlingId, simuleringResponsListe, ytelseType);

            utførSimuleringUtenInntrekk(simuleringRequestListe, simuleringGrunnlag);

            simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        } else {
            // Deaktiverer forrige simuleringgrunnlag hvis den nye simuleringen returnerer et tomt svar
            // f.eks. oppdrag finnes fra før, tom respons fra øk, FeilUnderBehandling som ikke er error
            // for å unngå å sende simuleringsresultat for forrige simulering når den nye simuleringen returnerer tomt resultat
            deaktiverBehandling(behandlingId);

        }
        LOG.info("Fullført simulering. behandlingID={} tidsforbrukTotalt={} ms", behandlingId, System.currentTimeMillis() - t0);
    }

    private void utførSimuleringUtenInntrekk(List<SimulerBeregningRequest> simuleringRequestListe, SimuleringGrunnlag simuleringGrunnlag) {
        BeregningResultat beregningResultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);

        if (erResultatMedInntrekkOgFeilutbetaling(beregningResultat)) {
            // Vi kommer nok aldri her så logger bare for å sjekke om teorien er riktig.
            LOG.info("Utfører simulering uten inntrekk.");
            List<SimulerBeregningRequest> requestsForBruker = finnRequestForBrukerOgSlåAvInntrekk(simuleringRequestListe, simuleringGrunnlag.getYtelseType());

            if (requestsForBruker.isEmpty()) {
                throw new IllegalStateException("Utviklerfeil: Skal alltid finne requests for bruker ved simuleringsresultat med inntrekk");
            }

            List<SimulerBeregningResponse> beregningResultater = utførSimulering(requestsForBruker);

            for (SimulerBeregningResponse response : beregningResultater) {
                if (response.getResponse() != null) {
                    Beregning beregning = response.getResponse().getSimulering();
                    resultatTransformer.mapSimuleringUtenInntrekk(beregning, simuleringGrunnlag);
                }
            }
        }
    }

    private boolean erResultatMedInntrekkOgFeilutbetaling(BeregningResultat beregningResultat) {
        BigDecimal inntrekkNesteUtbetaling = beregningResultat.getOppsummering().getInntrekkNesteUtbetaling();
        BigDecimal feilutbetaling = beregningResultat.getOppsummering().getFeilutbetaling();
        return inntrekkNesteUtbetaling != null && inntrekkNesteUtbetaling.compareTo(BigDecimal.ZERO) != 0
                && feilutbetaling != null && feilutbetaling.compareTo(BigDecimal.ZERO) != 0;
    }

    private List<SimulerBeregningRequest> finnRequestForBrukerOgSlåAvInntrekk(List<SimulerBeregningRequest> simuleringRequestListe, YtelseType ytelseType) {
        return simuleringRequestListe.stream()
                .filter(s -> Fagområde.utledFra(ytelseType).name().equals(s.getRequest().getOppdrag().getKodeFagomraade()))
                .filter(s -> s.getRequest().getOppdrag().getOppdragslinje() != null && !s.getRequest().getOppdrag().getOppdragslinje().isEmpty())
                .map(this::slåAvInntrekk)
                .collect(Collectors.toList());
    }

    private SimulerBeregningRequest slåAvInntrekk(SimulerBeregningRequest request) {
        Oppdrag oppdrag = request.getRequest().getOppdrag();
        Ompostering ompostering = OppdragMapper.mapOmpostering(oppdrag.getSaksbehId(), "N");
        oppdrag.setOmpostering(ompostering);
        oppdrag.setKodeEndring(KODE_ENDRING);
        return request;
    }

    public void kansellerSimulering(Long behandlingId) {
        deaktiverBehandling(behandlingId);
    }

    private void deaktiverBehandling(long behandlingId) {
        String deaktiverForLokalTesting = Environment.current().getProperty(DEAKTIVER_SIMULERING_DEAKTIVERING);
        if (deaktiverForLokalTesting == null || "false".equalsIgnoreCase(deaktiverForLokalTesting)) {
            Optional<SimuleringGrunnlag> eksisterende = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
            eksisterende.ifPresent(grunnlag -> {
                LOG.info("Deaktiverer simulering for behandling {}", behandlingId);
                simuleringRepository.deaktiverSimuleringGrunnlag(grunnlag);
            });
        }
    }

    private SimuleringGrunnlag transformerTilDatastruktur(long behandlingId, List<SimulerBeregningResponse> simuleringResponsListe, YtelseType ytelseType) {
        // Finn første gjelderId hvor responsen ikke er en null-verdi
        String gjelderIdFnr = simuleringResponsListe.stream()
                .map(SimulerBeregningResponse::getResponse)
                .filter(Objects::nonNull)
                .map(no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse::getSimulering)
                .filter(Objects::nonNull)
                .map(Beregning::getGjelderId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Utvikler-feil: skulle ikke kommet hit med bare null-responser"));

        String gjelderId = resultatTransformer.hentAktørIdHvisFnr(gjelderIdFnr);

        SimuleringResultat.Builder simuleringResultatBuilder = SimuleringResultat.builder();

        Map<String, SimuleringMottaker.Builder> mottakerBuilderMap = new HashMap<>();
        for (SimulerBeregningResponse response : simuleringResponsListe) {
            if (response.getResponse() == null || response.getResponse().getSimulering() == null) {
                continue;
            }
            Beregning beregning = response.getResponse().getSimulering();
            resultatTransformer.mapSimulering(mottakerBuilderMap, beregning);

        }
        mottakerBuilderMap.forEach((key, builder) -> simuleringResultatBuilder.medSimuleringMottaker(builder.build()));

        SimuleringGrunnlag.Builder simuleringGrunnlagBuilder = SimuleringGrunnlag.builder()
                .medEksternReferanse(new BehandlingRef(behandlingId))
                .medAktørId(gjelderId)
                .medSimuleringResultat(simuleringResultatBuilder.build())
                .medSimuleringKjørtDato(LocalDateTime.now())
                .medYtelseType(ytelseType);
        return simuleringGrunnlagBuilder.build();
    }

    private YtelseType bestemYtelseType(Long behandlingId, List<Oppdrag> oppdrag) {
        List<String> fagOmrådeKoder = oppdrag.stream()
                .map(no.nav.system.os.entiteter.oppdragskjema.Oppdrag::getKodeFagomraade)
                .distinct()
                .toList();
        if (fagOmrådeKoder.isEmpty()) {
            LOG.warn("Fant ingen fagområdeKoder for behandlingId={}", behandlingId);
            throw new IllegalStateException(String.format("Utvikler-feil: Ytelse Type må være satt for behandling: %s", behandlingId));
        }
        var ytelsetyper = fagOmrådeKoder.stream()
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

    private List<SimulerBeregningResponse> utførSimulering(List<SimulerBeregningRequest> simuleringRequestListe) {
        return simuleringRequestListe.stream()
                .map(this::utførSimulering)
                .toList();
    }

    private SimulerBeregningResponse utførSimulering(SimulerBeregningRequest request) {
        SimulerBeregningResponse simulerBeregningResponse = null;
        try {
            simulerBeregningResponse = oppdragConsumer.hentSimulerBeregningResponse(request);
        } catch (Exception e) {
            SECURE_LOGGER.info("Simulering feilet for request={}", anonymiser(request));
        }
        return simulerBeregningResponse;
    }

    private SimulerBeregningRequest anonymiser(SimulerBeregningRequest request) {
        request.getRequest().getOppdrag().setOppdragGjelderId(" *** ");
        request.getRequest().getOppdrag().getOppdragslinje().forEach(this::anonymiser);
        return request;
    }

    private void anonymiser(Oppdragslinje ol) {
        if (ol.getRefusjonsInfo() != null) {
            ol.getRefusjonsInfo().setRefunderesId(" *** ");}
    }

    private List<SimulerBeregningRequest> opprettBeregningRequestListe(List<Oppdrag> simuleringOppdragListe) {
        return simuleringOppdragListe.stream().map(oppdrag -> {
            no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest innerRequest = new no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.ObjectFactory().createSimulerBeregningRequest();
            innerRequest.setOppdrag(oppdrag);
            innerRequest.setSimuleringsPeriode(new no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest.SimuleringsPeriode());
            return lagRequest(innerRequest);
        }).collect(Collectors.toList());
    }

    private List<Oppdrag> konverterOppdragXmlTilSimuleringOppdrag(List<String> oppdragXmlListe) {
        return oppdragXmlListe.stream()
                .map(this::unmarshalOppdragOgKonverter)
                .collect(Collectors.toList());
    }


    private Oppdrag unmarshalOppdragOgKonverter(String oppdrag) {
        try {
            no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag fpOppdrag = JaxbHelper.unmarshalAndValidateXMLWithStAX(
                    OppdragSkjemaConstants.JAXB_CLASS, oppdrag, OppdragSkjemaConstants.XSD_LOCATION);
            return OppdragMapper.mapTilSimuleringOppdrag(fpOppdrag.getOppdrag110());
        } catch (JAXBException | SAXException | XMLStreamException e) {
            throw new TekniskException("FPO-832562", "Kunne ikke tolke mottatt oppdrag XML", e);
        }
    }

    private SimulerBeregningRequest lagRequest(no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest simulerBeregningRequest) {
        SimulerBeregningRequest request = new ObjectFactory().createSimulerBeregningRequest();
        request.setRequest(simulerBeregningRequest);
        return request;
    }
}
