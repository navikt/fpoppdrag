package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.FeilutbetalingTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverk.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverk.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverk.SatsType;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.BehandlingRef;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDetaljerDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringMottakerDto;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Dette class er for test tjeneste og må fjernes før lansering
 */
@ApplicationScoped
public class SimuleringTestTjenesteImpl implements SimuleringTestTjeneste {

    private SimuleringRepository simuleringRepository;

    private KodeverkRepository kodeverkRepository;

    private TpsTjeneste tpsTjeneste;

    private SimuleringTestTjenesteImpl() {
        // for CDI
    }

    @Inject
    public SimuleringTestTjenesteImpl(SimuleringRepository simuleringRepository,
                                      KodeverkRepository kodeverkRepository,
                                      TpsTjeneste tpsTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.kodeverkRepository = kodeverkRepository;
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public void lagreSimuleringTestData(SimuleringGjelderDto simuleringGjelderDto) {
        for (SimuleringDto simuleringDto : simuleringGjelderDto.getSimuleringer()) {
            SimuleringResultat.Builder simuleringResultatBuilder = SimuleringResultat.builder();

            for (SimuleringMottakerDto simuleringMottakerDto : simuleringDto.getSimuleringMottakerListe()) {
                simuleringResultatBuilder.medSimuleringMottaker(opprettSimuleringMottaker(simuleringMottakerDto));
            }

            SimuleringGrunnlag simuleringGrunnlag = SimuleringGrunnlag.builder()
                    .medEksternReferanse(new BehandlingRef(simuleringDto.getBehandlingId()))
                    .medAktørId(simuleringDto.getAktørId())
                    .medSimuleringResultat(simuleringResultatBuilder.build())
                    .medSimuleringKjørtDato(FPDateUtil.nå())
                    .medYtelseType(bestemYtelseType(simuleringGjelderDto))
                    .build();
            simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        }
    }

    private SimuleringMottaker opprettSimuleringMottaker(SimuleringMottakerDto simuleringMottakerDto) {
        SimuleringMottaker.Builder simuleringMottakerBuilder = SimuleringMottaker.builder()
                .medMottakerNummer(finnMottakerId(simuleringMottakerDto))
                .medMottakerType(kodeverkRepository.finn(MottakerType.class, simuleringMottakerDto.getMottakerType()));

        for (SimuleringDetaljerDto simuleringDetaljerDto : simuleringMottakerDto.getSimuleringResultatDetaljer()) {
            simuleringMottakerBuilder.medSimulertPostering(opprettSimulertPostering(simuleringDetaljerDto));
        }
        return simuleringMottakerBuilder.build();
    }

    private SimulertPostering opprettSimulertPostering(SimuleringDetaljerDto simuleringDetaljerDto) {
        SimulertPostering.Builder simPostbuilder = SimulertPostering.builder().medFom(simuleringDetaljerDto.getFom())
                .medTom(simuleringDetaljerDto.getTom())
                .medFagOmraadeKode(kodeverkRepository.finn(FagOmrådeKode.class, simuleringDetaljerDto.getFagomraadeKode()))
                .medKonto(simuleringDetaljerDto.getKonto())
                .medBeløp(simuleringDetaljerDto.getBeløp())
                .medBetalingType(kodeverkRepository.finn(BetalingType.class, simuleringDetaljerDto.getBetalingType()))
                .medPosteringType(kodeverkRepository.finn(PosteringType.class, simuleringDetaljerDto.getPosteringType()))
                .medKlasseKode(kodeverkRepository.finn(KlasseKode.class, simuleringDetaljerDto.getKlasseKode()))
                .medForfallsdato(simuleringDetaljerDto.getForfallsdato())
                .utenInntrekk(simuleringDetaljerDto.isUtenInntrekk());
        if (simuleringDetaljerDto.getHarDagsats()) {
            simPostbuilder.medSatsType(kodeverkRepository.finn(SatsType.class, SatsType.DAG.getKode()));
            int antallVirkedager = FeilutbetalingTjeneste.finnAntallVirkedager(simuleringDetaljerDto.getFom(), simuleringDetaljerDto.getTom());
            if (antallVirkedager > 0) {
                BigDecimal beløp = simuleringDetaljerDto.getBeløp();
                simPostbuilder.medSats(beløp.divide(BigDecimal.valueOf(antallVirkedager), RoundingMode.HALF_UP));
            } else {
                simPostbuilder.medSats(BigDecimal.ZERO);
            }
        }
        return simPostbuilder.build();
    }

    private YtelseType bestemYtelseType(SimuleringGjelderDto simuleringGjelderDto) {
        List<String> fagområdekoder = simuleringGjelderDto.getSimuleringer().stream()
                .flatMap(s -> s.getSimuleringMottakerListe().stream())
                .flatMap(m -> m.getSimuleringResultatDetaljer().stream())
                .map(SimuleringDetaljerDto::getFagomraadeKode).distinct()
                .collect(Collectors.toList());

        if (fagområdekoder.stream().anyMatch(FagOmrådeKode::gjelderEngangsstønad)) {
            return YtelseType.ENGANGSTØNAD;
        }
        if (fagområdekoder.stream().anyMatch(FagOmrådeKode::gjelderForeldrepenger)) {
            return YtelseType.FORELDREPENGER;
        }
        if (fagområdekoder.stream().anyMatch(FagOmrådeKode::gjelderSvangerskapspenger)) {
            return YtelseType.SVANGERSKAPSPENGER;
        }
        return YtelseType.UDEFINERT;
    }

    private String finnMottakerId(SimuleringMottakerDto simuleringMottakerDto) {
        String mottakerId = simuleringMottakerDto.getMottakerId();
        String mottakerType = simuleringMottakerDto.getMottakerType();
        if (MottakerType.ARBG_PRIV.getKode().equals(mottakerType)) {
            if (!PersonIdent.erGyldigFnr(mottakerId)) {
                throw new IllegalArgumentException("Mottaker av type " + mottakerType + " har ikke et gyldig fødselsnummer");
            }
            Optional<AktørId> funnetAktørId = tpsTjeneste.hentAktørForFnr(PersonIdent.fra(mottakerId));
            return funnetAktørId.map(AktørId::getId).orElseThrow(() -> new IllegalArgumentException("Fant ikke aktørId for mottaker av type " + mottakerType));
        }
        if (MottakerType.BRUKER.getKode().equals(mottakerType)) {
            return null; //Trenger ikke lagre aktørId for brukeren
        }
        return mottakerId;
    }
}
