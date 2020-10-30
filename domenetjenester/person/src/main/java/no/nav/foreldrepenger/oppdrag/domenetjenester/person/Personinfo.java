package no.nav.foreldrepenger.oppdrag.domenetjenester.person;

public class Personinfo {
    private PersonIdent personIdent;
    private String navn;

    public Personinfo(PersonIdent personIdent, String navn) {
        this.navn = navn;
        this.personIdent = personIdent;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    public String getNavn() {
        return navn;
    }

}
