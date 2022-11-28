package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy;

// TODO: Konsolider FeilDto fra web modul. Flytt til felles? kontrakter?
public record FeilDto(FeilType type, String feilmelding) {
}
