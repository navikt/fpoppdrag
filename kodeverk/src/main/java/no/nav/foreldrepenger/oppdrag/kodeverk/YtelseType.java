package no.nav.foreldrepenger.oppdrag.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "YtelseType")
@DiscriminatorValue(YtelseType.DISCRIMINATOR)
public class YtelseType extends Kodeliste {
    public static final String DISCRIMINATOR = "YTELSE_TYPE";

    public static final YtelseType ENGANGSTØNAD = new YtelseType("ES");
    public static final YtelseType FORELDREPENGER = new YtelseType("FP");
    public static final YtelseType SVANGERSKAPSPENGER = new YtelseType("SVP");
    public static final YtelseType UDEFINERT = new YtelseType("-");

    YtelseType() {
        // For hibernate
    }

    public YtelseType(String kode) {
        super(kode, DISCRIMINATOR);
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