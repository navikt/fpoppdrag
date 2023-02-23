package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SimuleringGjelderDto implements AbacDto {

    //TODO slå sammen SimuleringGjelderDto og SimuleringDto
    @Valid
    @Size(min = 1)
    private List<SimuleringDto> simuleringer;

    private SimuleringGjelderDto() {
        //CDI
    }

    public SimuleringGjelderDto(List<SimuleringDto> simuleringer) {
        this.simuleringer = simuleringer;
    }

    public List<SimuleringDto> getSimuleringer() {
        return simuleringer;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett();
        simuleringer.forEach(simuleringDto -> abacDataAttributter.leggTil(simuleringDto.abacAttributter()));
        return abacDataAttributter;
    }
}
