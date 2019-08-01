package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import java.util.Collection;

public class Valideringsfeil extends RuntimeException {
    private final Collection<FeltFeilDto> feltfeil;

    public Valideringsfeil(Collection<FeltFeilDto> feltfeil) {
        this.feltfeil = feltfeil;
    }

    public Collection<FeltFeilDto> getFeltFeil() {
        return feltfeil;
    }

}
