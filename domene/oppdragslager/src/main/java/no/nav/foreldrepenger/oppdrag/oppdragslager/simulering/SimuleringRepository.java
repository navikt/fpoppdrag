package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.Optional;

public interface SimuleringRepository {

    void lagreSimuleringGrunnlag(SimuleringGrunnlag simuleringGrunnlag);

    void deaktiverSimuleringGrunnlag(SimuleringGrunnlag simuleringGrunnlag);

    Optional<SimuleringGrunnlag> hentSimulertOppdragForBehandling(Long behandlingId);
}
