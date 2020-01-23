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
public enum YtelseType implements Kodeverdi {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    public static final String KODEVERK = "YTELSE_TYPE";

    private static final Map<String, YtelseType> KODER = new LinkedHashMap<>();

    private String kode;

    YtelseType() {
        // Hibernate trenger den
    }

    private YtelseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static YtelseType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static YtelseType fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
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
    public static class KodeverdiConverter implements AttributeConverter<YtelseType, String> {
        @Override
        public String convertToDatabaseColumn(YtelseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public YtelseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    public boolean gjelderForeldrePenger() {
        return FORELDREPENGER.equals(this);
    }

    public boolean gjelderEngangsstønad() {
        return ENGANGSTØNAD.equals(this);
    }

    public boolean gjelderSvangerskapspenger() {
        return SVANGERSKAPSPENGER.equals(this);
    }
}
