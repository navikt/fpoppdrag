package no.nav.foreldrepenger.oppdrag.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BetalingType implements Kodeverdi {

    DEBIT("D"),
    KREDIT("K"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, BetalingType> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    BetalingType() {
        // Hibernate trenger den
    }

    private BetalingType(String kode) {
        this.kode = kode;
    }

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
    public static class KodeverdiConverter implements AttributeConverter<BetalingType, String> {
        @Override
        public String convertToDatabaseColumn(BetalingType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BetalingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }

        private static BetalingType fraKode(String kode) {
            if (kode == null) {
                return null;
            }
            return Optional.ofNullable(KODER.get(kode))
                    .orElseThrow(() -> new IllegalArgumentException("Ukjent Betalingtype: " + kode));
        }


    }

}
