package no.nav.foreldrepenger.oppdrag.domenetjenester.person;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonIdentTest {

    @Test
    void gyldigFoedselsnummer_Fnr() {
        var syntetiskFnr = "22305014919";
        var gyldig = PersonIdent.erGyldigFnr(syntetiskFnr);
        assertThat(gyldig).isTrue();

        assertThat(new PersonIdent(syntetiskFnr).erDnr()).isFalse();
    }

    @Test
    void gyldigFoedselsnummer_Dnr() {
        var dnr = "65038300827";
        var gyldig = PersonIdent.erGyldigFnr(dnr);
        assertThat(gyldig).isTrue();

        assertThat(new PersonIdent(dnr).erDnr()).isTrue();
    }

    @Test
    void ugyldigFoedselsnummer() {
        var foedselsnummer = "12345678910";
        var gyldig = PersonIdent.erGyldigFnr(foedselsnummer);
        assertThat(gyldig).isFalse();

        foedselsnummer = "9999999999";
        gyldig = PersonIdent.erGyldigFnr(foedselsnummer);
        assertThat(gyldig).isFalse();
    }
}
