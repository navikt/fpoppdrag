package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
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

/**
 * Dette class er for test tjeneste og må fjernes før lansering
 */
@ApplicationScoped
public class SimuleringTestTjeneste {

    private SimuleringRepository simuleringRepository;
    private TpsTjeneste tpsTjeneste;

    SimuleringTestTjeneste() {
        // for CDI
    }

    @Inject
    public SimuleringTestTjeneste(SimuleringRepository simuleringRepository,
                                  TpsTjeneste tpsTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.tpsTjeneste = tpsTjeneste;
    }

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
                    .medSimuleringKjørtDato(LocalDateTime.now())
                    .medYtelseType(bestemYtelseType(simuleringGjelderDto))
                    .build();
            simuleringRepository.lagreSimuleringGrunnlag(simuleringGrunnlag);
        }
    }

    private SimuleringMottaker opprettSimuleringMottaker(SimuleringMottakerDto simuleringMottakerDto) {
        SimuleringMottaker.Builder simuleringMottakerBuilder = SimuleringMottaker.builder()
                .medMottakerNummer(finnMottakerId(simuleringMottakerDto))
                .medMottakerType(MottakerType.fraKodeDefaultUdefinert(simuleringMottakerDto.getMottakerType()));

        for (SimuleringDetaljerDto simuleringDetaljerDto : simuleringMottakerDto.getSimuleringResultatDetaljer()) {
            simuleringMottakerBuilder.medSimulertPostering(opprettSimulertPostering(simuleringDetaljerDto));
        }
        return simuleringMottakerBuilder.build();
    }

    private SimulertPostering opprettSimulertPostering(SimuleringDetaljerDto simuleringDetaljerDto) {
        SimulertPostering.Builder simPostbuilder = SimulertPostering.builder().medFom(simuleringDetaljerDto.getFom())
                .medTom(simuleringDetaljerDto.getTom())
                .medFagOmraadeKode(FagOmrådeKode.fraKodeDefaultUdefinert(simuleringDetaljerDto.getFagomraadeKode()))
                .medBeløp(simuleringDetaljerDto.getBeløp())
                .medBetalingType(BetalingType.fraKodeDefaultUdefinert(simuleringDetaljerDto.getBetalingType()))
                .medPosteringType(PosteringType.fraKodeDefaultUdefinert(simuleringDetaljerDto.getPosteringType()))
                .medForfallsdato(simuleringDetaljerDto.getForfallsdato())
                .utenInntrekk(simuleringDetaljerDto.isUtenInntrekk());

        return simPostbuilder.build();
    }

    private YtelseType bestemYtelseType(SimuleringGjelderDto simuleringGjelderDto) {
        List<String> fagområdekoder = simuleringGjelderDto.getSimuleringer().stream()
                .flatMap(s -> s.getSimuleringMottakerListe().stream())
                .flatMap(m -> m.getSimuleringResultatDetaljer().stream())
                .map(SimuleringDetaljerDto::getFagomraadeKode).distinct()
                .collect(Collectors.toList());

        Set<YtelseType> ytelsetyper = fagområdekoder.stream()
                .map(FagOmrådeKode::fraKode)
                .map(FagOmrådeKode::getYtelseType)
                .collect(Collectors.toSet());

        if (ytelsetyper.isEmpty()) {
            return YtelseType.UDEFINERT;
        }
        if (ytelsetyper.size() == 1) {
            return ytelsetyper.iterator().next();
        }
        throw new IllegalArgumentException("Kan ikke finne unik ytelsetype fra fagområdekoder: " + fagområdekoder);
    }

    private String finnMottakerId(SimuleringMottakerDto simuleringMottakerDto) {
        String mottakerId = simuleringMottakerDto.getMottakerId();
        String mottakerType = simuleringMottakerDto.getMottakerType();
        if (MottakerType.ARBG_PRIV.getKode().equals(mottakerType)) {
            if (!PersonIdent.erGyldigFnr(mottakerId)) {
                throw new IllegalArgumentException("Mottaker av type " + mottakerType + " har ikke et gyldig fødselsnummer");
            }
            Optional<AktørId> funnetAktørId = tpsTjeneste.hentAktørIdForPersonIdent(PersonIdent.fra(mottakerId));
            return funnetAktørId.map(AktørId::getId).orElseThrow(() -> new IllegalArgumentException("Fant ikke aktørId for mottaker av type " + mottakerType));
        }
        if (MottakerType.BRUKER.getKode().equals(mottakerType)) {
            return null; //Trenger ikke lagre aktørId for brukeren
        }
        return mottakerId;
    }
}
