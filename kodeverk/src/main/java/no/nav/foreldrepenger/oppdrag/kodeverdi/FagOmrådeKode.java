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
public enum FagOmrådeKode implements Kodeverdi {

    ENGANGSSTØNAD("REFUTG", YtelseType.ENGANGSTØNAD),
    FORELDREPENGER("FP", YtelseType.FORELDREPENGER),
    FORELDREPENGER_ARBEIDSGIVER("FPREF", YtelseType.FORELDREPENGER),
    SYKEPENGER("SP", YtelseType.SYKEPENGER),
    SYKEPENGER_ARBEIDSGIVER("SPREF", YtelseType.SYKEPENGER),
    SVANGERSKAPSPENGER("SVP", YtelseType.SVANGERSKAPSPENGER),
    SVANGERSKAPSPENGER_ARBEIDSGIVER("SVPREF", YtelseType.SVANGERSKAPSPENGER),
    PLEIEPENGER_SYKT_BARN("PB", YtelseType.PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_SYKT_BARN_ARBEIDSGIVER("PBREF", YtelseType.PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_NÆRSTÅENDE("PN", YtelseType.PLEIEPENGER_NÆRSTÅENDE),
    PLEIEPENGER_NÆRSTÅENDE_ARBEIDSGIVER("PNREF", YtelseType.PLEIEPENGER_NÆRSTÅENDE),
    OMSORGSPENGER("OM", YtelseType.OMSORGSPENGER),
    OMSORGSPENGER_ARBEIDSGIVER("OMREF", YtelseType.OMSORGSPENGER),
    OPPLÆRINGSPENGER("OPP", YtelseType.OPPLÆRINGSPENGER),
    OPPLÆRINGSPENGER_ARBEIDSGIVER("OPPREF", YtelseType.OPPLÆRINGSPENGER),
    PLEIEPENGER_V1("OOP", YtelseType.PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_V1_ARBEIDSGIVER("OOPREF", YtelseType.PLEIEPENGER_SYKT_BARN),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-", YtelseType.UDEFINERT),
    ;

    private static final Map<String, FagOmrådeKode> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;
    private YtelseType ytelseType;

    FagOmrådeKode() {
        // Hibernate trenger den
    }

    private FagOmrådeKode(String kode, YtelseType ytelseType) {
        this.kode = kode;
        this.ytelseType = ytelseType;
    }

    public static FagOmrådeKode fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
                .orElseThrow(() -> new IllegalArgumentException("Ukjent Fagområdekode: " + kode));
    }

    public static FagOmrådeKode fraKodeDefaultUdefinert(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    @Override
    public String getKode() {
        return kode;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
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

    public static FagOmrådeKode getFagOmrådeKodeForBrukerForYtelseType(YtelseType ytelseType) {
        for (var fk : values()) {
            if (ytelseType.equals(fk.getYtelseType())) {
                return fk;
            }
        }
        throw new IllegalArgumentException("Utvikler-feil: Mangler mapping mellom ytelsetype og FagOmrådeKode for bruker. Ytelsetype=" + ytelseType);
    }
}
