package no.nav.foreldrepenger.oppdrag.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FagOmrådeKode implements Kodeverdi {

    FORELDREPENGER("FP"),
    FORELDREPENGER_ARBEIDSGIVER("FPREF"),
    SYKEPENGER("SP"),
    SYKEPENGER_ARBEIDSGIVER("SPREF"),
    PLEIEPENGER("OOP"),
    PLEIEPENGER_ARBEIDSGIVER("OOPREF"),
    ENGANGSSTØNAD("REFUTG"),
    SVANGERSKAPSPENGER("SVP"),
    SVANGERSKAPSPENGER_ARBEIDSGIVER("SVPREF"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    public static final String KODEVERK = "FAG_OMRAADE_KODE";

    private static final Map<String, FagOmrådeKode> KODER = new LinkedHashMap<>();

    private String kode;

    FagOmrådeKode() {
        // Hibernate trenger den
    }

    private FagOmrådeKode(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static FagOmrådeKode fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static FagOmrådeKode fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<FagOmrådeKode, String> {
        @Override
        public String convertToDatabaseColumn(FagOmrådeKode attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public FagOmrådeKode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    public static boolean gjelderForeldrepenger(String fagOmrådeKode) {
        return FORELDREPENGER.getKode().equals(fagOmrådeKode) || FORELDREPENGER_ARBEIDSGIVER.getKode().equals(fagOmrådeKode);
    }

    public static boolean gjelderEngangsstønad(String fagOmrådeKode) {
        return ENGANGSSTØNAD.getKode().equals(fagOmrådeKode);
    }

    public static boolean gjelderSvangerskapspenger(String fagOmrådeKode) {
        return SVANGERSKAPSPENGER.getKode().equals(fagOmrådeKode) || SVANGERSKAPSPENGER_ARBEIDSGIVER.getKode().equals(fagOmrådeKode);
    }

    public static FagOmrådeKode getFagOmrådeKodeForBrukerForYtelseType(YtelseType ytelseType) {
        if (ytelseType.gjelderEngangsstønad()) {
            return ENGANGSSTØNAD;
        }
        if (ytelseType.gjelderSvangerskapspenger()) {
            return SVANGERSKAPSPENGER;
        }
        if (ytelseType.gjelderForeldrePenger()) {
            return FORELDREPENGER;
        }
        if (ytelseType.equals(YtelseType.UDEFINERT)) {
            return FagOmrådeKode.UDEFINERT;
        }
        throw new IllegalArgumentException("Utvikler-feil: Mangler mapping mellom ytelsetype og fagområdekode for bruker. Ytelsetype=" + ytelseType);
    }
}
