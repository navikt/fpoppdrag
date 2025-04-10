package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

/**
 * AbacAttributtTyper som er i bruk i FPSAK.
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    BEHANDLING_ID;

    @Override
    public boolean getMaskerOutput() {
        return false;
    }
}
