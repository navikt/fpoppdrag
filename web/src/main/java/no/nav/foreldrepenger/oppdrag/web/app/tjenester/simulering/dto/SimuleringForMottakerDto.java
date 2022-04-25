package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.YtelseUtleder;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

public class SimuleringForMottakerDto {

    private MottakerType mottakerType;
    private String mottakerNummer;
    private String mottakerNavn;
    private String mottakerIdentifikator;
    private List<SimuleringResultatPerFagområdeDto> resultatPerFagområde = new ArrayList<>();
    private List<SimuleringResultatRadDto> resultatOgMotregningRader = new ArrayList<>();
    private LocalDate nesteUtbPeriodeFom;
    private LocalDate nestUtbPeriodeTom;


    public MottakerType getMottakerType() {
        return mottakerType;
    }

    public String getMottakerNummer() {
        return mottakerNummer;
    }

    public String getMottakerNavn() {
        return mottakerNavn;
    }

    public String getMottakerIdentifikator() {
        return mottakerIdentifikator;
    }

    public LocalDate getNesteUtbPeriodeFom() {
        return nesteUtbPeriodeFom;
    }

    public LocalDate getNestUtbPeriodeTom() {
        return nestUtbPeriodeTom;
    }

    public List<SimuleringResultatPerFagområdeDto> getResultatPerFagområde() {
        return Collections.unmodifiableList(resultatPerFagområde);
    }

    public List<SimuleringResultatRadDto> getResultatOgMotregningRader() {
        return Collections.unmodifiableList(resultatOgMotregningRader);
    }

    public static class Builder {

        private Map<Fagområde, Integer> SORTERING = new HashMap<>();

        private SimuleringForMottakerDto kladd = new SimuleringForMottakerDto();
        private YtelseType gjelderYtelsetype;

        public Builder medResultatPerFagområde(List<SimuleringResultatPerFagområdeDto> resultatPerFagområde) {
            kladd.resultatPerFagområde = resultatPerFagområde;
            return this;
        }

        public Builder medResultatOgMotregningRader(List<SimuleringResultatRadDto> resultatOgMotregningRader) {
            kladd.resultatOgMotregningRader = resultatOgMotregningRader;
            return this;
        }

        public Builder medMottakerType(MottakerType mottakerType) {
            kladd.mottakerType = mottakerType;
            return this;
        }

        public Builder medMottakerIdentifikator(String mottakerIdentifikator) {
            kladd.mottakerIdentifikator = mottakerIdentifikator;
            return this;
        }

        public Builder medMottakerNummer(String mottakerNummer) {
            kladd.mottakerNummer = mottakerNummer;
            return this;
        }

        public Builder medMottakerNavn(String mottakerNavn) {
            kladd.mottakerNavn = mottakerNavn;
            return this;
        }

        public Builder medNesteUtbetalingsperiode(Periode periode) {
            kladd.nesteUtbPeriodeFom = periode.getPeriodeFom();
            kladd.nestUtbPeriodeTom = periode.getPeriodeTom();
            return this;
        }

        public SimuleringForMottakerDto build() {
            initSortering(gjelderYtelsetype);
            kladd.resultatPerFagområde.sort(Comparator.comparingInt(o -> getSortering(o.getFagOmrådeKode())));
            kladd.resultatOgMotregningRader.sort(Comparator.comparingInt(o -> o.getFeltnavn().ordinal()));
            return kladd;
        }

        private void initSortering(YtelseType gjelderYtelsetype) {
            //default sortering
            SORTERING.put(Fagområde.REFUTG, 101);
            SORTERING.put(Fagområde.SVP, 101);
            SORTERING.put(Fagområde.SVPREF, 102);
            SORTERING.put(Fagområde.FP, 103);
            SORTERING.put(Fagområde.FPREF, 104);
            SORTERING.put(Fagområde.SP, 105);
            SORTERING.put(Fagområde.SPREF, 106);
            SORTERING.put(Fagområde.OOP, 107);
            SORTERING.put(Fagområde.OOPREF, 108);
            SORTERING.put(Fagområde.PB, 109);
            SORTERING.put(Fagområde.PBREF, 110);
            SORTERING.put(Fagområde.PN, 111);
            SORTERING.put(Fagområde.PNREF, 112);
            SORTERING.put(Fagområde.OM, 113);
            SORTERING.put(Fagområde.OMREF, 114);
            SORTERING.put(Fagområde.OPP, 115);
            SORTERING.put(Fagområde.OPPREF, 116);

            //flytter gjeldende ytelsetype først i sorteringen
            for (Fagområde fagOmrådeKode : SORTERING.keySet()) {
                if (YtelseUtleder.utledFor(fagOmrådeKode).equals(gjelderYtelsetype)) {
                    SORTERING.put(fagOmrådeKode, SORTERING.get(fagOmrådeKode) - 100);
                }
            }
        }

        private int getSortering(Fagområde fagomradeKode) {
            return SORTERING.getOrDefault(fagomradeKode, Integer.MAX_VALUE);
        }

        public Builder medGjelderYtelseType(YtelseType gjelderYtelseType) {
            this.gjelderYtelsetype = gjelderYtelseType;
            return this;
        }
    }
}
