package no.nav.foreldrepenger.oppdrag.web.app.validering;

import java.util.Objects;

import jakarta.validation.ConstraintValidatorContext;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Kodeverdi;

public class KodeverdiValidator extends KodeverkValidator<Kodeverdi> {

    @Override
    public boolean isValid(Kodeverdi kodeverdi, ConstraintValidatorContext context) {
        if (Objects.equals(null, kodeverdi)) {
            return true;
        }
        boolean ok = true;

        if (!gyldigKode(kodeverdi.getKode())) {
            context.buildConstraintViolationWithTemplate(invKode);
            ok = false;
        }

        return ok;
    }
}
