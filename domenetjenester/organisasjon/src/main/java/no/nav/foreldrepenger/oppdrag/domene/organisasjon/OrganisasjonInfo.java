package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

public class OrganisasjonInfo {

    private String orgnummer;

    private String navn;

    public OrganisasjonInfo(String orgnummer, String navn) {
        this.orgnummer = orgnummer;
        this.navn = navn;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public String getNavn() {
        return navn;
    }
}
