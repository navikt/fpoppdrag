package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverk.YtelseType;

public class SimuleringForMottakerDto {

    private MottakerType mottakerType;
    private String mottakerNummer;
    private String mottakerNavn;
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

        private Map<FagOmrådeKode, Integer> SORTERING = new HashMap<>();

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
            SORTERING.put(FagOmrådeKode.ENGANGSSTØNAD, 3);
            SORTERING.put(FagOmrådeKode.SVANGERSKAPSPENGER, 4);
            SORTERING.put(FagOmrådeKode.SVANGERSKAPSPENGER_ARBEIDSGIVER, 5);
            SORTERING.put(FagOmrådeKode.FORELDREPENGER, 6);
            SORTERING.put(FagOmrådeKode.FORELDREPENGER_ARBEIDSGIVER, 7);
            SORTERING.put(FagOmrådeKode.SYKEPENGER, 8);
            SORTERING.put(FagOmrådeKode.SYKEPENGER_ARBEIDSGIVER, 9);
            SORTERING.put(FagOmrådeKode.PLEIEPENGER, 10);
            SORTERING.put(FagOmrådeKode.PLEIEPENGER_ARBEIDSGIVER, 11);

            if (YtelseType.SVANGERSKAPSPENGER.equals(gjelderYtelsetype)) {
                SORTERING.replace(FagOmrådeKode.SVANGERSKAPSPENGER, 1);
                SORTERING.replace(FagOmrådeKode.SVANGERSKAPSPENGER_ARBEIDSGIVER, 2);
            } else if (YtelseType.FORELDREPENGER.equals(gjelderYtelsetype)) {
                SORTERING.replace(FagOmrådeKode.FORELDREPENGER, 1);
                SORTERING.replace(FagOmrådeKode.FORELDREPENGER_ARBEIDSGIVER, 2);
            }
        }

        private int getSortering(FagOmrådeKode fagomradeKode) {
            return SORTERING.getOrDefault(fagomradeKode, Integer.MAX_VALUE);
        }

        public Builder medGjelderYtelseType(YtelseType gjelderYtelseType) {
            this.gjelderYtelsetype = gjelderYtelseType;
            return this;
        }
    }
}
