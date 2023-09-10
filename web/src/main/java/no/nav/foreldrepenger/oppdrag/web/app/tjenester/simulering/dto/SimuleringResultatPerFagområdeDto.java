package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.List;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;

public record SimuleringResultatPerFagområdeDto(Fagområde fagOmrådeKode, List<SimuleringResultatRadDto> rader) { }
