package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;

public class SimuleringResultatPerFagområdeDto {

    private Fagområde fagOmrådeKode;
    private List<SimuleringResultatRadDto> rader = new ArrayList<>();

    public Fagområde getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public List<SimuleringResultatRadDto> getRader() {
        return Collections.unmodifiableList(rader);
    }

    public static class Builder {
        SimuleringResultatPerFagområdeDto kladd = new SimuleringResultatPerFagområdeDto();

        public Builder medFagområdeKode(Fagområde fagOmrådeKode) {
            kladd.fagOmrådeKode = fagOmrådeKode;
            return this;
        }

        public Builder medRader(List<SimuleringResultatRadDto> rader) {
            kladd.rader = rader;
            return this;
        }

        public SimuleringResultatPerFagområdeDto build() {
            kladd.rader.sort(Comparator.comparingInt(o -> o.getFeltnavn().ordinal()));
            return kladd;
        }
    }
}
