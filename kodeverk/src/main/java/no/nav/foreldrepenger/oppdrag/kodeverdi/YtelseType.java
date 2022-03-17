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

public enum YtelseType implements Kodeverdi {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),
    SYKEPENGER("SP"),

    PLEIEPENGER_SYKT_BARN("PSB"),
    PLEIEPENGER_NÆRSTÅENDE("PPN"),
    OMSORGSPENGER("OMP"),
    OPPLÆRINGSPENGER("OLP"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, YtelseType> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    YtelseType() {
        // Hibernate trenger den
    }

    private YtelseType(String kode) {
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
    public static class KodeverdiConverter implements AttributeConverter<YtelseType, String> {
        @Override
        public String convertToDatabaseColumn(YtelseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public YtelseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }

        private static YtelseType fraKode(String kode) {
            if (kode == null) {
                return null;
            }
            return Optional.ofNullable(KODER.get(kode))
                    .orElseThrow(() -> new IllegalArgumentException("Ukjent Ytelsetype: " + kode));
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
