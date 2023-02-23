package no.nav.foreldrepenger.oppdrag.web.app.exceptions;


import java.util.List;

import no.nav.vedtak.exception.FunksjonellException;

class FeltValideringFeil {

    static FunksjonellException feltverdiKanIkkeValideres(List<String> feltnavn) {
        return new FunksjonellException("FPO-328673",
                String.format("Det oppstod en valideringsfeil på felt %s. Vennligst kontroller at alle feltverdier er korrekte.", feltnavn));
    }
}
