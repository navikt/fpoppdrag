package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SimuleringResultatRadDto {
    private RadId feltnavn;
    private List<SimuleringResultatPerMånedDto> resultaterPerMåned = new ArrayList<>();

    public RadId getFeltnavn() {
        return feltnavn;
    }

    public List<SimuleringResultatPerMånedDto> getResultaterPerMåned() {
        return Collections.unmodifiableList(resultaterPerMåned);
    }

    public static class Builder {
        SimuleringResultatRadDto kladd = new SimuleringResultatRadDto();

        public Builder medFeltnavn(RadId feltnavn) {
            kladd.feltnavn = feltnavn;
            return this;
        }

        public Builder medResultaterPerMåned(List<SimuleringResultatPerMånedDto> resultaterPerMåned) {
            kladd.resultaterPerMåned = resultaterPerMåned;
            return this;
        }

        public Builder medResultatPerMåned(SimuleringResultatPerMånedDto simuleringResultatPerMånedDto) {
            kladd.resultaterPerMåned.add(simuleringResultatPerMånedDto);
            return this;
        }

        public SimuleringResultatRadDto build() {
            kladd.resultaterPerMåned.sort(Comparator.comparing(p -> p.periode().fom()));
            return kladd;
        }
    }

}
