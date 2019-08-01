package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ØkonomioppdragRepository {

    Oppdragskontroll hentOppdragskontroll(long oppdragskontrollId);

    Oppdragskontroll finnVentendeOppdrag(long behandlingId);

    Optional<Oppdragskontroll> finnOppdragForBehandling(long behandlingId);

    long lagre(Oppdragskontroll oppdragskontroll);

    List<Oppdrag110> hentOppdrag110ForPeriodeOgFagområde(LocalDate fomDato, LocalDate tomDato, String fagområde);

    List<Oppdragskontroll> finnAlleOppdragForSak(String saksnr);
}
