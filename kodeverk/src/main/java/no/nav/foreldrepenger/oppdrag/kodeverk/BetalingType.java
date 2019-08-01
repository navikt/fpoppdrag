package no.nav.foreldrepenger.oppdrag.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "BetalingType")
@DiscriminatorValue(BetalingType.DISCRIMINATOR)
public class BetalingType extends Kodeliste {
    public static final String DISCRIMINATOR = "BETALING_TYPE";

    public static final BetalingType DEBIT = new BetalingType("D");

    public static final BetalingType KREDIT = new BetalingType("K");

    BetalingType() {
        // Hibernate
    }

    private BetalingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
