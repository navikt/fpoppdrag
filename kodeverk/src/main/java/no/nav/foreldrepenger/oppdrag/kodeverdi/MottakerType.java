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
public enum MottakerType implements Kodeverdi {

    BRUKER("BRUKER"),
    ARBG_ORG("ARBG_ORG"),
    ARBG_PRIV("ARBG_PRIV"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    public static final String KODEVERK = "MOTTAKER_TYPE";

    private static final Map<String, MottakerType> KODER = new LinkedHashMap<>();

    private String kode;

    MottakerType() {
        // Hibernate trenger den
    }

    private MottakerType(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static MottakerType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static MottakerType fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
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
    public static class KodeverdiConverter implements AttributeConverter<MottakerType, String> {
        @Override
        public String convertToDatabaseColumn(MottakerType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public MottakerType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
