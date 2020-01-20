package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static no.nav.vedtak.util.Objects.check;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragSkjemaConstants;
import no.nav.foreldrepenger.oppdrag.OppdragConsumer;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.OppdragMapper;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.SimuleringResultatTransformer;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringXml;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringXmlRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag.ØkonomiKodeEndringLinje;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.entiteter.beregningskjema.Beregning;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa;
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode;
import no.nav.system.os.entiteter.oppdragskjema.Ompostering;
import no.nav.system.os.tjenester.simulerfpservice.feil.FeilUnderBehandling;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.ObjectFactory;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.konfig.PropertyUtil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class StartSimuleringTjenesteImpl implements StartSimuleringTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(StartSimuleringTjenesteImpl.class);
    private static final String DEAKTIVER_SIMULERING_DEAKTIVERING = "testing.deaktiver.simulering.deaktivering";

    private static final Logger LOG = LoggerFactory.getLogger(StartSimuleringTjenesteImpl.class);
    private SimuleringXmlRepository simuleringXmlRepository;
    private SimuleringRepository simuleringRepository;
    private OppdragConsumer oppdragConsumer;
    private SimuleringResultatTransformer resultatTransformer;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    StartSimuleringTjenesteImpl() {
        // CDI
    }

    @Inject
    public StartSimuleringTjenesteImpl(SimuleringXmlRepository simuleringXmlRepository,
                                       SimuleringRepository simuleringRepository,
                                       OppdragConsumer oppdragConsumer,
                                       SimuleringResultatTransformer resultatTransformer, SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this.simuleringXmlRepository = simuleringXmlRepository;
        this.simuleringRepository = simuleringRepository;
        this.oppdragConsumer = oppdragConsumer;
        this.resultatTransformer = resultatTransformer;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
    }

    private static boolean harIkkeTomRespons(List<SimulerBeregningResponse> simuleringResponsListe) {
        return simuleringResponsListe.stream()
                .map(SimulerBeregningResponse::getResponse)
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

    @Override
    public void startSimulering(Long behandlingId, List<String> oppdragXmlListe) {
        long t0 = System.currentTimeMillis();
        String totalStørrelseUt = størrelse(oppdragXmlListe);
        LOG.info("Starter simulering. behandlingID={} oppdragantall={} totalstørrelseUt={}",
                behandlingId,
                oppdragXmlListe.size(),
                totalStørrelseUt);

        List<Oppdrag> simuleringOppdragListe = konverterOppdragXmlTilSimuleringOppdrag(oppdragXmlListe);
        List<SimulerBeregningRequest> simuleringRequestListe = opprettBeregningRequestListe(simuleringOppdragListe);

        List<SimuleringXml.Builder> xmlBuilderList = lagSimuleringXmlEntitetBuilder(behandlingId, oppdragXmlListe, simuleringRequestListe);
        lagreSimuleringXml(xmlBuilderList);


        //TODO skal ikke ha ny transaksjon, bruk savepoint istedet.. ønsker å oppnå med dette at requestXML lagres også når det feiler
        simuleringXmlRepository.nyTransaksjon();

        List<SimulerBeregningResponse> simuleringResponsListe = utførSimulering(simuleringRequestListe);

        LOG.info("Simulering svarmeldinger mottatt. behandlingID={} oppdragantall={} totalstørrelseUt={} tidsforbruk={} ms",
                behandlingId,
                oppdragXmlListe.size(),
                totalStørrelseUt,
                System.currentTimeMillis() - t0);

        if (harIkkeTomRespons(simuleringResponsListe)) {
            oppdaterXmlBuilder(behandlingId, xmlBuilderList, simuleringResponsListe);
            lagreSimuleringXml(xmlBuilderList);

            YtelseType ytelseType = bestemYtelseType(behandlingId, simuleringOppdragListe);
            SimuleringGrunnlag simuleringGrunnlag = transformerTilDatastruktur(behandlingId, simuleringResponsListe, ytelseType);

            utførSimuleringUtenInntrekk(behandlingId, simuleringRequestListe, simuleringGrunnlag);

            simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        } else {
            // Deaktiverer forrige simuleringgrunnlag hvis den nye simuleringen returnerer et tomt svar
            // f.eks. oppdrag finnes fra før, tom respons fra øk, FeilUnderBehandling som ikke er error
            // for å unngå å sende simuleringsresultat for forrige simulering når den nye simuleringen returnerer tomt resultat
            deaktiverBehandling(behandlingId);

        }
        LOG.info("Fullført simulering. behandlingID={} tidsforbrukTotalt={} ms", behandlingId, System.currentTimeMillis() - t0);
    }

    private void utførSimuleringUtenInntrekk(Long behandlingId, List<SimulerBeregningRequest> simuleringRequestListe, SimuleringGrunnlag simuleringGrunnlag) {
        BeregningResultat beregningResultat = simuleringBeregningTjeneste.hentBeregningsresultat(simuleringGrunnlag);

        if (erResultatMedInntrekkOgFeilutbetaling(beregningResultat)) {
            List<SimulerBeregningRequest> requestsForBruker = finnRequestForBrukerOgSlåAvInntrekk(simuleringRequestListe, simuleringGrunnlag.getYtelseType());

            if (requestsForBruker.isEmpty()) {
                throw new IllegalStateException("Utviklerfeil: Skal alltid finne requests for bruker ved simuleringsresultat med inntrekk");
            }
            List<SimuleringXml.Builder> xmlBuilderListe = lagSimuleringXmlEntitetBuilder(behandlingId, requestsForBruker);

            List<SimulerBeregningResponse> beregningResultater = utførSimulering(requestsForBruker);

            for (SimulerBeregningResponse response : beregningResultater) {
                if (response.getResponse() != null) {
                    Beregning beregning = response.getResponse().getSimulering();
                    resultatTransformer.mapSimuleringUtenInntrekk(beregning, simuleringGrunnlag);
                }
            }
            oppdaterXmlBuilder(behandlingId, xmlBuilderListe, beregningResultater);
            lagreSimuleringXml(xmlBuilderListe);
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
                .filter(s -> FagOmrådeKode.getFagOmrådeKodeForBrukerForYtelseType(ytelseType).getKode().equals(s.getRequest().getOppdrag().getKodeFagomraade()))
                .filter(s -> s.getRequest().getOppdrag().getOppdragslinje() != null && !s.getRequest().getOppdrag().getOppdragslinje().isEmpty())
                .map(this::slåAvInntrekk)
                .collect(Collectors.toList());
    }

    private SimulerBeregningRequest slåAvInntrekk(SimulerBeregningRequest request) {
        Oppdrag oppdrag = request.getRequest().getOppdrag();
        Ompostering ompostering = OppdragMapper.mapOmpostering(oppdrag.getSaksbehId(), "N");
        oppdrag.setOmpostering(ompostering);
        oppdrag.setKodeEndring(ØkonomiKodeEndringLinje.ENDR.name());
        return request;
    }

    @Override
    public void kansellerSimulering(Long behandlingId) {
        deaktiverBehandling(behandlingId);
    }

    private void deaktiverBehandling(long behandlingId) {
        String deaktiverForLokalTesting = PropertyUtil.getProperty(DEAKTIVER_SIMULERING_DEAKTIVERING);
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
                .findFirst()
                .map(m -> m
                        .getSimulering()
                        .getGjelderId())
                .orElseThrow(() -> new IllegalArgumentException("Utvikler-feil: skulle ikke kommet hit med bare null-responser"));

        String gjelderId = resultatTransformer.hentAktørIdHvisFnr(gjelderIdFnr);

        SimuleringResultat.Builder simuleringResultatBuilder = SimuleringResultat.builder();

        Map<String, SimuleringMottaker.Builder> mottakerBuilderMap = new HashMap<>();
        for (SimulerBeregningResponse response : simuleringResponsListe) {
            if (response.getResponse() == null) {
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
                .medSimuleringKjørtDato(FPDateUtil.nå())
                .medYtelseType(ytelseType);
        return simuleringGrunnlagBuilder.build();
    }

    private YtelseType bestemYtelseType(Long behandlingId, List<Oppdrag> oppdrag) {
        List<String> fagOmrådeKoder = oppdrag.stream().map(no.nav.system.os.entiteter.oppdragskjema.Oppdrag::getKodeFagomraade).distinct().collect(Collectors.toList());
        if (fagOmrådeKoder.isEmpty()) {
            LOG.warn("Fant ingen fagområdeKoder for behandlingId={}", behandlingId);
            return YtelseType.UDEFINERT;
        }
        if (fagOmrådeKoder.stream().anyMatch(FagOmrådeKode::gjelderEngangsstønad)) {
            return YtelseType.ENGANGSTØNAD;
        }
        if (fagOmrådeKoder.stream().anyMatch(FagOmrådeKode::gjelderForeldrepenger)) {
            return YtelseType.FORELDREPENGER;
        }
        if (fagOmrådeKoder.stream().anyMatch(FagOmrådeKode::gjelderSvangerskapspenger)) {
            return YtelseType.SVANGERSKAPSPENGER;
        }
        throw StartSimuleringTjenesteFeil.FACTORY.manglerMappingMellomFagområdeKodeOgYtleseType(behandlingId, fagOmrådeKoder).toException();
    }

    private List<SimulerBeregningResponse> utførSimulering(List<SimulerBeregningRequest> simuleringRequestListe) {
        return simuleringRequestListe.stream()
                .map(this::utførSimulering)
                .collect(Collectors.toList());
    }

    private SimulerBeregningResponse utførSimulering(SimulerBeregningRequest simuleringOppdrag) {
        try {
            return oppdragConsumer.hentSimulerBeregningResponse(simuleringOppdrag);
        } catch (SimulerBeregningFeilUnderBehandling e) {
            FeilUnderBehandling fault = e.getFaultInfo();
            throw StartSimuleringTjenesteFeil.FACTORY.feilUnderBehandlingAvSimulering(fault.getErrorSource(), fault.getErrorType(), fault.getErrorMessage(), fault.getRootCause(), fault.getDateTimeStamp(), e).toException();
        } catch (WebServiceException e) {
            throw StartSimuleringTjenesteFeil.FACTORY.feilUnderKallTilSimuleringtjeneste(e).toException();
        }
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

    private void lagreSimuleringXml(List<SimuleringXml.Builder> builders) {
        builders.forEach(builder -> simuleringXmlRepository.lagre(builder.build()));
    }

    private List<SimuleringXml.Builder> lagSimuleringXmlEntitetBuilder(long behandlingId, List<SimulerBeregningRequest> beregningRequestListe) {
        List<SimuleringXml.Builder> builders = new ArrayList<>();
        beregningRequestListe.forEach(request -> {
            String marshalled = SimuleringMarshaller.marshall(behandlingId, request);
            builders.add(SimuleringXml.builder().medEksternReferanse(behandlingId).medRequest(marshalled));
        });
        return builders;
    }

    private List<SimuleringXml.Builder> lagSimuleringXmlEntitetBuilder(long behandlingId, List<String> fpsakInput, List<SimulerBeregningRequest> beregningRequestListe) {
        // Begge listene skal være i samme rekkefølge
        List<SimuleringXml.Builder> builders = lagSimuleringXmlEntitetBuilder(behandlingId, beregningRequestListe);
        check(fpsakInput.size() == builders.size(), "Ulik lengde på input og requests for behandlingId=" + behandlingId);
        for (int i = 0; i < fpsakInput.size(); i++) {
            SimuleringXml.Builder builder = builders.get(i);
            builder.medFpsakInput(fpsakInput.get(i));
        }
        return builders;
    }

    private void oppdaterXmlBuilder(long behandlingId, List<SimuleringXml.Builder> builderList, List<SimulerBeregningResponse> simuleringResponse) {
        // Begge listene skal være i samme rekkefølge,
        for (int i = 0; i < builderList.size(); i++) {
            SimulerBeregningResponse respons = simuleringResponse.get(i);
            SimuleringXml.Builder builder = builderList.get(i);
            korrigerForEvtManglendeFagsystemId(behandlingId, respons);
            builder.medResponse(SimuleringMarshaller.marshall(behandlingId, respons));
        }
    }

    private static void korrigerForEvtManglendeFagsystemId(long behandlingId, SimulerBeregningResponse respons) {
        if (respons == null || respons.getResponse() == null || respons.getResponse().getSimulering() == null) {
            return;
        }
        Beregning simulering = respons.getResponse().getSimulering();
        for (BeregningsPeriode periode : simulering.getBeregningsPeriode()) {
            for (BeregningStoppnivaa stoppnivå : periode.getBeregningStoppnivaa()) {
                if (stoppnivå.getFagsystemId() == null || stoppnivå.getFagsystemId().isEmpty()) {
                    stoppnivå.setFagsystemId("FEIL-MANGLET"); //setter fagsystemId til for å kunne marshalle
                    StartSimuleringTjenesteFeil.FACTORY.mangletFagsystemId(behandlingId, periode.getPeriodeFom(), periode.getPeriodeTom()).log(logger);
                }
            }
        }
    }

    private Oppdrag unmarshalOppdragOgKonverter(String oppdrag) {
        try {
            no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag fpOppdrag = JaxbHelper.unmarshalAndValidateXMLWithStAX(
                    OppdragSkjemaConstants.JAXB_CLASS, oppdrag, OppdragSkjemaConstants.XSD_LOCATION);
            return OppdragMapper.mapTilSimuleringOppdrag(fpOppdrag.getOppdrag110());
        } catch (JAXBException | SAXException | XMLStreamException e) {
            throw StartSimuleringTjenesteFeil.FACTORY.kunneIkkeUnmarshalleOppdragXml(e).toException();
        }
    }

    private SimulerBeregningRequest lagRequest(no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest simulerBeregningRequest) {
        SimulerBeregningRequest request = new ObjectFactory().createSimulerBeregningRequest();
        request.setRequest(simulerBeregningRequest);
        return request;
    }
}
