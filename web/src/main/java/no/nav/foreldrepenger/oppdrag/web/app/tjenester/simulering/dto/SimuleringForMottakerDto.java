package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
    private PeriodeDto nesteUtbPeriode;


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

    public PeriodeDto getNesteUtbPeriode() {
        return nesteUtbPeriode;
    }

    public List<SimuleringResultatPerFagområdeDto> getResultatPerFagområde() {
        return Collections.unmodifiableList(resultatPerFagområde);
    }

    public List<SimuleringResultatRadDto> getResultatOgMotregningRader() {
        return Collections.unmodifiableList(resultatOgMotregningRader);
    }

    public static class Builder {

        private final Map<Fagområde, Integer> sortering = new EnumMap<>(Fagområde.class);
        private final SimuleringForMottakerDto kladd = new SimuleringForMottakerDto();
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

        public Builder medNesteUtbetalingsperiode(LocalDate fom, LocalDate tom) {
            kladd.nesteUtbPeriodeFom = fom;
            kladd.nestUtbPeriodeTom = tom;
            kladd.nesteUtbPeriode = new PeriodeDto(fom, tom);
            return this;
        }

        public SimuleringForMottakerDto build() {
            if (kladd.nesteUtbPeriode == null) {
                kladd.nesteUtbPeriode = new PeriodeDto(null, null);
            }
            initSortering(gjelderYtelsetype);
            kladd.resultatPerFagområde.sort(Comparator.comparingInt(o -> getSortering(o.fagOmrådeKode())));
            kladd.resultatOgMotregningRader.sort(Comparator.comparingInt(o -> o.getFeltnavn().ordinal()));
            return kladd;
        }

        private void initSortering(YtelseType gjelderYtelsetype) {
            //default sortering
            sortering.put(Fagområde.REFUTG, 101);
            sortering.put(Fagområde.SVP, 101);
            sortering.put(Fagområde.SVPREF, 102);
            sortering.put(Fagområde.FP, 103);
            sortering.put(Fagområde.FPREF, 104);
            sortering.put(Fagområde.SP, 105);
            sortering.put(Fagområde.SPREF, 106);
            sortering.put(Fagområde.OOP, 107);
            sortering.put(Fagområde.OOPREF, 108);
            sortering.put(Fagområde.PB, 109);
            sortering.put(Fagområde.PBREF, 110);
            sortering.put(Fagområde.PN, 111);
            sortering.put(Fagområde.PNREF, 112);
            sortering.put(Fagområde.OM, 113);
            sortering.put(Fagområde.OMREF, 114);
            sortering.put(Fagområde.OPP, 115);
            sortering.put(Fagområde.OPPREF, 116);

            //flytter gjeldende ytelsetype først i sorteringen
            sortering.forEach((fagområde, value) -> {
                if (YtelseUtleder.utledFor(fagområde).equals(gjelderYtelsetype)) {
                    sortering.put(fagområde, sortering.get(fagområde) - 100);
                }
            });
        }

        private int getSortering(Fagområde fagomradeKode) {
            return sortering.getOrDefault(fagomradeKode, Integer.MAX_VALUE);
        }

        public Builder medGjelderYtelseType(YtelseType gjelderYtelseType) {
            this.gjelderYtelsetype = gjelderYtelseType;
            return this;
        }
    }
}
