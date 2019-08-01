package no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PersonIdentTest {

    @Test
    public void gyldigFoedselsnummer_Fnr() {
        String fnr = "07078518434";
        boolean gyldig = PersonIdent.erGyldigFnr(fnr);
        assertThat(gyldig).isEqualTo(true);

        assertThat(new PersonIdent(fnr).erDnr()).isFalse();
    }

    @Test
    public void gyldigFoedselsnummer_Dnr() {
        String dnr = "65038300827";
        boolean gyldig = PersonIdent.erGyldigFnr(dnr);
        assertThat(gyldig).isEqualTo(true);

        assertThat(new PersonIdent(dnr).erDnr()).isTrue();
    }

    @Test
    public void ugyldigFoedselsnummer() {
        String foedselsnummer = "31048518434";
        boolean gyldig = PersonIdent.erGyldigFnr(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);

        foedselsnummer = "9999999999";
        gyldig = PersonIdent.erGyldigFnr(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);
    }
}