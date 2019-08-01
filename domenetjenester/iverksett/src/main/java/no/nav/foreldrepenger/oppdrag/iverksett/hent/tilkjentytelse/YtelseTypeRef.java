package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.YtelseType;

@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface YtelseTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike ytelse typer.
     * <p>
     * Må matche ett innslag i YtelseType enum for å kunne kjøres.
     *
     * @see no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.YtelseType
     */
    String value();

    /**
     * AnnotationLiteral som kan brukes ved CDI søk.
     */
    class YtelseTypeRefLiteral extends AnnotationLiteral<YtelseTypeRef> implements YtelseTypeRef {

        private String navn;

        public YtelseTypeRefLiteral(YtelseType ytelseType) {
            this.navn = ytelseType.getKode();
        }

        @Override
        public String value() {
            return navn;
        }
    }
}
