package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Attestant180;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Grad170;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Ompostering116;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsEnhet120;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsLinje150;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeArbeidsgiver;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeStatusLinje;
import no.nav.system.os.entiteter.oppdragskjema.Attestant;
import no.nav.system.os.entiteter.oppdragskjema.Enhet;
import no.nav.system.os.entiteter.oppdragskjema.Grad;
import no.nav.system.os.entiteter.oppdragskjema.Ompostering;
import no.nav.system.os.entiteter.oppdragskjema.RefusjonsInfo;
import no.nav.system.os.entiteter.typer.simpletypes.FradragTillegg;
import no.nav.system.os.entiteter.typer.simpletypes.KodeArbeidsgiver;
import no.nav.system.os.entiteter.typer.simpletypes.KodeStatusLinje;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.ObjectFactory;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje;
import no.nav.vedtak.util.FPDateUtil;

public class OppdragMapper {

    public static final String PATTERN = "yyyy-MM-dd";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

    private OppdragMapper() {
        // skal ikke kunne instansiere klassen
    }

    /**
     * Mapper oppdrag-110 sendt fra FPSAK til oppdrag som FPOPPDRAG sender til økonomi.
     *
     * @param oppdrag110
     * @return
     */
    public static Oppdrag mapTilSimuleringOppdrag(no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag110 oppdrag110) {
        Oppdrag oppdrag = new ObjectFactory().createOppdrag();

        oppdrag.setKodeEndring(oppdrag110.getKodeEndring());
        oppdrag.setKodeFagomraade(oppdrag110.getKodeFagomraade());
        oppdrag.setFagsystemId(oppdrag110.getFagsystemId());
        oppdrag.setUtbetFrekvens(oppdrag110.getUtbetFrekvens());
        oppdrag.setOppdragGjelderId(oppdrag110.getOppdragGjelderId());
        oppdrag.setDatoOppdragGjelderFom(convDate(oppdrag110.getDatoOppdragGjelderFom()));
        oppdrag.setSaksbehId(oppdrag110.getSaksbehId());
        if (oppdrag110.getOmpostering116() != null) {
            oppdrag.setOmpostering(mapOmpostering(oppdrag110.getOmpostering116()));
        }

        oppdrag.getEnhet().addAll(mapOppdragsEnhet120(oppdrag110.getOppdragsEnhet120()));
        oppdrag.getOppdragslinje().addAll(mapOppdragslinje150(oppdrag110.getOppdragsLinje150()));

        return oppdrag;
    }

    public static Ompostering mapOmpostering(String saksbehId, String ompostering) {
        Ompostering op = new Ompostering();
        op.setOmPostering(ompostering);
        op.setSaksbehId(saksbehId);
        op.setTidspktReg(tilSpesialkodetDatoOgKlokkeslett(FPDateUtil.nå()));
        return op;
    }

    private static Ompostering mapOmpostering(Ompostering116 ompostering116) {
        Ompostering op = mapOmpostering(ompostering116.getSaksbehId(), ompostering116.getOmPostering());
        if (ompostering116.getDatoOmposterFom() != null) {
            op.setDatoOmposterFom(convDate(ompostering116.getDatoOmposterFom()));
        }
        return op;
    }

    private static String tilSpesialkodetDatoOgKlokkeslett(LocalDateTime dt) {
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSS";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dt.format(dtf);
    }

    private static List<Enhet> mapOppdragsEnhet120(List<OppdragsEnhet120> oppdragsEnhet120Liste) {
        return oppdragsEnhet120Liste.stream().map(enhet120 -> {
            Enhet enhet = new Enhet();
            enhet.setDatoEnhetFom(convDate(enhet120.getDatoEnhetFom()));
            enhet.setEnhet(enhet120.getEnhet());
            enhet.setTypeEnhet(enhet120.getTypeEnhet());
            return enhet;
        }).collect(Collectors.toList());
    }

    private static List<Oppdragslinje> mapOppdragslinje150(List<OppdragsLinje150> oppdragsLinje150Liste) {
        return oppdragsLinje150Liste.stream()
                .map(OppdragMapper::mapOppdragslinje150)
                .collect(Collectors.toList());
    }

    private static Oppdragslinje mapOppdragslinje150(OppdragsLinje150 oppdragsLinje150) {
        Oppdragslinje oppdragslinje = new Oppdragslinje();

        // mapper enkeltelementer
        oppdragslinje.setKodeEndringLinje(oppdragsLinje150.getKodeEndringLinje());
        oppdragslinje.setVedtakId(oppdragsLinje150.getVedtakId());
        oppdragslinje.setDelytelseId(oppdragsLinje150.getDelytelseId());
        oppdragslinje.setKodeKlassifik(oppdragsLinje150.getKodeKlassifik());
        oppdragslinje.setDatoVedtakFom(convDate(oppdragsLinje150.getDatoVedtakFom()));
        oppdragslinje.setDatoVedtakTom(convDate(oppdragsLinje150.getDatoVedtakTom()));
        oppdragslinje.setSats(oppdragsLinje150.getSats());
        oppdragslinje.setFradragTillegg(mapTfradragTillegg(oppdragsLinje150.getFradragTillegg()));
        oppdragslinje.setTypeSats(oppdragsLinje150.getTypeSats());
        oppdragslinje.setBrukKjoreplan(oppdragsLinje150.getBrukKjoreplan());
        oppdragslinje.setSaksbehId(oppdragsLinje150.getSaksbehId());
        oppdragslinje.setHenvisning(oppdragsLinje150.getHenvisning());

        // mapper lister
        oppdragslinje.getGrad().addAll(mapGrad170(oppdragsLinje150.getGrad170()));
        oppdragslinje.getAttestant().addAll(mapAttestant180(oppdragsLinje150.getAttestant180()));

        // mapper elementer som kan være null
        if (oppdragsLinje150.getKodeArbeidsgiver() != null) {
            oppdragslinje.setKodeArbeidsgiver(mapTkodeArbeidsgiver(oppdragsLinje150.getKodeArbeidsgiver()));
        } else {
            oppdragslinje.setUtbetalesTilId(oppdragsLinje150.getUtbetalesTilId());
        }
        if (oppdragsLinje150.getRefusjonsinfo156() != null) {
            oppdragslinje.setRefusjonsInfo(mapRefusjonsinfo156(oppdragsLinje150.getRefusjonsinfo156()));
        }
        if (oppdragsLinje150.getRefFagsystemId() != null) {
            oppdragslinje.setRefFagsystemId(oppdragsLinje150.getRefFagsystemId());
        }
        if (oppdragsLinje150.getRefDelytelseId() != null) {
            oppdragslinje.setRefDelytelseId(oppdragsLinje150.getRefDelytelseId());
        }
        if (oppdragsLinje150.getDatoStatusFom() != null) {
            oppdragslinje.setDatoStatusFom(convDate(oppdragsLinje150.getDatoStatusFom()));
        }
        if (oppdragsLinje150.getKodeStatusLinje() != null) {
            oppdragslinje.setKodeStatusLinje(mapTkodeStatusLinje(oppdragsLinje150.getKodeStatusLinje()));
        }

        return oppdragslinje;
    }

    private static RefusjonsInfo mapRefusjonsinfo156(Refusjonsinfo156 refusjonsinfo156) {
        RefusjonsInfo refusjonsInfo = new RefusjonsInfo();
        refusjonsInfo.setMaksDato(convDate(refusjonsinfo156.getMaksDato()));
        refusjonsInfo.setDatoFom(convDate(refusjonsinfo156.getDatoFom()));
        refusjonsInfo.setRefunderesId(refusjonsinfo156.getRefunderesId());
        return refusjonsInfo;
    }

    private static List<Grad> mapGrad170(List<Grad170> grad170Liste) {
        return grad170Liste.stream().map(grad170 -> {
            Grad grad = new Grad();
            grad.setGrad(grad170.getGrad());
            grad.setTypeGrad(grad170.getTypeGrad());
            return grad;
        }).collect(Collectors.toList());
    }

    private static List<Attestant> mapAttestant180(List<Attestant180> attestant180Liste) {
        return attestant180Liste.stream().map(attestant180 -> {
            Attestant attestant = new Attestant();
            attestant.setAttestantId(attestant180.getAttestantId());
            if (attestant180.getDatoUgyldigFom() != null) {
                attestant.setDatoUgyldigFom(convDate(attestant180.getDatoUgyldigFom()));
            }
            return attestant;
        }).collect(Collectors.toList());
    }

    private static FradragTillegg mapTfradragTillegg(TfradragTillegg tfradragTillegg) {
        return FradragTillegg.fromValue(tfradragTillegg.value());
    }

    private static KodeArbeidsgiver mapTkodeArbeidsgiver(TkodeArbeidsgiver tkodeArbeidsgiver) {
        return KodeArbeidsgiver.fromValue(tkodeArbeidsgiver.value());
    }

    private static KodeStatusLinje mapTkodeStatusLinje(TkodeStatusLinje tkodeStatusLinje) {
        return KodeStatusLinje.fromValue(tkodeStatusLinje.value());
    }

    private static String convDate(XMLGregorianCalendar xmlGregorianCalendar) {
        LocalDate ld = xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
        return ld.format(formatter);
    }
}
