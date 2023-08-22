package no.nav.foreldrepenger.oppdrag.web.app.validering;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;

import no.nav.vedtak.util.InputValideringRegex;

public abstract class KodeverkValidator<T> implements ConstraintValidator<ValidKodeverk, T> {

    static final String invKode = "kodeverks kode feilet validering"; // NOSONAR
    static final String invNavn = "kodeverks navn feilet validering"; // NOSONAR

    Pattern kodeverkPattern = Pattern.compile(InputValideringRegex.KODEVERK);

    @Override
    public void initialize(ValidKodeverk validKodeverk) {
        // ikke noe å gjøre
    }

    boolean erTomEllerNull(String str) {
        return (Objects.equals(null, str) || str.isEmpty());
    }

    boolean gyldigKode(String kode) {
        return (!erTomEllerNull(kode) && gyldigLengde(kode, 1, 100) && kodeverkPattern.matcher(kode).matches());
    }

    boolean gyldigKodeverk(String kodeverk) {
        return (!erTomEllerNull(kodeverk) && gyldigLengde(kodeverk, 0, 256) && kodeverkPattern.matcher(kodeverk).matches());
    }

    boolean gyldigLengde(String str, int min, int max) {
        return (str.length() >= min && str.length() <= max);
    }

}
