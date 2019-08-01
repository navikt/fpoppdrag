package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;

public class SimuleringResultatPerFagområdeDto {

    private FagOmrådeKode fagOmrådeKode;
    private List<SimuleringResultatRadDto> rader = new ArrayList<>();

    public FagOmrådeKode getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public List<SimuleringResultatRadDto> getRader() {
        return Collections.unmodifiableList(rader);
    }

    public static class Builder {
        SimuleringResultatPerFagområdeDto kladd = new SimuleringResultatPerFagområdeDto();

        public Builder medFagområdeKode(FagOmrådeKode fagOmrådeKode) {
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
