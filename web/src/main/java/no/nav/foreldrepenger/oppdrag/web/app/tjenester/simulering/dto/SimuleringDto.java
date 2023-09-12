package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SimuleringDto(DetaljertSimuleringResultatDto simuleringResultat,
                            DetaljertSimuleringResultatDto simuleringResultatUtenInntrekk,
                            boolean slåttAvInntrekk) {


    public record DetaljertSimuleringResultatDto(PeriodeDto periode, boolean ingenPerioderMedAvvik,
                                                 Long sumEtterbetaling, Long sumFeilutbetaling, Long sumInntrekk,
                                                 List<SimuleringForMottakerDto> perioderPerMottaker) {
    }


    public record SimuleringForMottakerDto(KontraktMottakerType mottakerType, String mottakerNummer, String mottakerIdentifikator,
                                           List<SimuleringResultatPerFagområdeDto> resultatPerFagområde,
                                           List<SimuleringResultatRadDto> resultatOgMotregningRader,
                                           PeriodeDto nesteUtbPeriode) {
    }


    public record SimuleringResultatPerFagområdeDto(KontraktFagområde fagOmrådeKode, List<SimuleringResultatRadDto> rader) { }

    public record SimuleringResultatRadDto(RadId feltnavn, List<SimuleringResultatPerMånedDto> resultaterPerMåned) { }

    public record SimuleringResultatPerMånedDto(PeriodeDto periode, Long beløp) {

        public SimuleringResultatPerMånedDto(LocalDate fom, LocalDate tom, BigDecimal beløp) {
            this(new PeriodeDto(fom , tom), beløp != null ? beløp.longValue() : null);
        }

    }
}
