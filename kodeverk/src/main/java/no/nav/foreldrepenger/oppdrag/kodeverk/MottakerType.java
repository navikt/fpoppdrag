package no.nav.foreldrepenger.oppdrag.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "MottakerType")
@DiscriminatorValue(MottakerType.DISCRIMINATOR)
public class MottakerType extends Kodeliste {

    public static final String DISCRIMINATOR = "MOTTAKER_TYPE";

    public static final MottakerType BRUKER = new MottakerType("BRUKER"); //$NON-NLS-1$

    public static final MottakerType ARBG_ORG = new MottakerType("ARBG_ORG"); //$NON-NLS-1$

    public static final MottakerType ARBG_PRIV = new MottakerType("ARBG_PRIV"); //$NON-NLS-1$

    MottakerType() {
        // Hibernate
    }

    private MottakerType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
