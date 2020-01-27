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
public enum KlasseKode implements Kodeverdi {

    FPATAL("FPATAL"),
    FPATFER("FPATFER"),
    FPATFRI("FPATFRI"),
    FPATORD("FPATORD"),
    FPENAD_OP("FPENAD-OP"),
    FPENFOD_OP("FPENFOD-OP"),
    FPREFAGFER_IOP("FPREFAGFER-IOP"),
    FPREFAG_IOP("FPREFAG-IOP"),
    FPSNDDM_OP("FPSNDDM-OP"),
    FPSNDFI("FPSNDFI"),
    FPSNDJB_OP("FPSNDJB-OP"),
    FPSND_OP("FPSND-OP"),
    FSKTSKAT("FSKTSKAT"),
    KL_KODE_FEIL_KORTTID("KL_KODE_FEIL_KORTTID"),
    TBMOTOBS("TBMOTOBS"),
    SPSND100D1DAGPFI("SPSND100D1DAGPFI"),
    SPSND100D1DTRPFI("SPSND100D1DTRPFI"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    public static final String KODEVERK = "KLASSE_KODE";

    private static final Map<String, KlasseKode> KODER = new LinkedHashMap<>();

    private String kode;

    KlasseKode() {
        // Hibernate trenger den
    }

    private KlasseKode(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static KlasseKode fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Klassekode: " + kode);
        }
        return ad;
    }

    public static KlasseKode fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
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
    public static class KodeverdiConverter implements AttributeConverter<KlasseKode, String> {
        @Override
        public String convertToDatabaseColumn(KlasseKode attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public KlasseKode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}
