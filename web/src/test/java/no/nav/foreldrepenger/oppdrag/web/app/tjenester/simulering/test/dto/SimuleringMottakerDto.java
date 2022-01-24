package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class SimuleringMottakerDto {

    @NotNull
    @Digits(integer = 11, fraction = 0, message = "ugyldig mottaker")
    private String mottakerId;

    @NotNull
    @Size(max = 100, message = "mottakerType er for høyt")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String mottakerType;

    @Valid
    @Size(min = 1)
    private List<SimuleringDetaljerDto> simuleringResultatDetaljer;

    private SimuleringMottakerDto() {
        //CDI
    }

    public SimuleringMottakerDto(String mottakerId, String mottakerType, List<SimuleringDetaljerDto> simuleringResultatDetaljer) {
        this.mottakerId = mottakerId;
        this.mottakerType = mottakerType;
        this.simuleringResultatDetaljer = simuleringResultatDetaljer;
    }

    public String getMottakerId() {
        return mottakerId;
    }

    public String getMottakerType() {
        return mottakerType;
    }

    public List<SimuleringDetaljerDto> getSimuleringResultatDetaljer() {
        return simuleringResultatDetaljer;
    }
}