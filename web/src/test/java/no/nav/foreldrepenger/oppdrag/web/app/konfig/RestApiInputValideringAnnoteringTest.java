package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Context;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.vedtak.felles.testutilities.cdi.WeldContext;

class RestApiInputValideringAnnoteringTest extends RestApiTester {

    static {
        WeldContext.getInstance(); // init cdi container
    }

    private final Function<Method, String> printKlasseOgMetodeNavn = (method -> String.format("%s.%s", method.getDeclaringClass(), method.getName()));

    /**
     * IKKE ignorer denne testen, den sørger for at inputvalidering er i orden for REST_MED_INNTEKTSMELDING-grensesnittene
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her     *
     */
    @Test
    void alle_parametre_i_REST_api_skal_ha_gyldig_type_og_annotert_med_valid() {
        for (Method method : finnAlleRestMetoder()) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                assertThat(method.getParameterTypes()[i].isAssignableFrom(String.class)).as("REST_MED_INNTEKTSMELDING-metoder skal ikke har parameter som er String eller mer generelt. Bruk DTO-er og valider. " + printKlasseOgMetodeNavn.apply(method)).isFalse();
                assertThat(isRequiredAnnotationPresent(method.getParameters()[i])).as("Alle parameter for REST_MED_INNTEKTSMELDING-metoder skal være annotert med @Valid. Var ikke det for " + printKlasseOgMetodeNavn.apply(method)).withFailMessage("Fant parametere som mangler @Valid annotation '" + method.getParameters()[i].toString() + "'").isTrue();
            }
        }
    }

    /**
     * IKKE ignorer denne testen, den sørger for at inputvalidering er i orden for REST_MED_INNTEKTSMELDING-grensesnittene
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her
     */
    @Test
    void alle_felter_i_objekter_som_brukes_som_inputDTO_skal_enten_ha_valideringsannotering_eller_være_av_godkjent_type() {
        var dtoKlasser = finnAlleDtoTyper();
        var validerteKlasser = new HashSet<Class<?>>(); //trengs for å unngå løkker og unngå å validere samme klasse flere ganger dobbelt
        for (var c : dtoKlasser) {
            validerRekursivt(validerteKlasser, c, null);
        }
    }

    private boolean isRequiredAnnotationPresent(Parameter parameter) {
        final var validAnnotation = parameter.getAnnotation(Valid.class);
        if (validAnnotation == null) {
            final var contextAnnotation = parameter.getAnnotation(Context.class);
            return contextAnnotation != null;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private static final Map<Class, List<List<Class<? extends Annotation>>>> VALIDERINGSALTERNATIVER = new HashMap<>() {{
        put(String.class, asList(List.of(Pattern.class),
                asList(Pattern.class, Size.class),
                singletonList(Digits.class)));
        put(Long.class, singletonList(
                asList(Min.class, Max.class)));
        put(long.class, singletonList(
                asList(Min.class, Max.class)));
        put(Integer.class, singletonList(
                asList(Min.class, Max.class)));
        put(int.class, singletonList(
                asList(Min.class, Max.class)));

        //følgende unnntas i praksis. LocalDate og LocalDateTime har egne deserializers
        put(boolean.class, singletonList(emptyList()));
        put(Boolean.class, singletonList(emptyList()));
        put(LocalDate.class, singletonList(emptyList()));
        put(LocalDateTime.class, singletonList(emptyList()));
        put(BigDecimal.class, asList(
                asList(Min.class, Max.class, Digits.class),
                asList(DecimalMin.class, DecimalMax.class, Digits.class)));
    }};

    private static List<List<Class<? extends Annotation>>> getVurderingsalternativer(Class<?> klasse) {
        if (Collection.class.isAssignableFrom(klasse) || Map.class.isAssignableFrom(klasse)) {
            return singletonList(singletonList(Size.class));
        }
        return VALIDERINGSALTERNATIVER.get(klasse);
    }

    private Set<Class<?>> finnAlleDtoTyper() {
        Set<Class<?>> parametre = new TreeSet<>(Comparator.comparing(Class::getName));
        for (var method : finnAlleRestMetoder()) {
            parametre.addAll(Arrays.asList(method.getParameterTypes()));
            for (var type : method.getGenericParameterTypes()) {
                if (type instanceof ParameterizedType genericTypes) {
                    for (var gen : genericTypes.getActualTypeArguments()) {
                        parametre.add((Class<?>) gen);
                    }
                }
            }
        }
        Set<Class<?>> filtreteParametre = new TreeSet<>(Comparator.comparing(Class::getName));
        for (var klasse : parametre) {
            if (klasse.getName().startsWith("java")) {
                //ikke sjekk nedover i innebygde klasser, det skal brukes annoteringer på tidligere tidspunkt
                continue;
            }
            filtreteParametre.add(klasse);
        }
        return filtreteParametre;
    }

    private static void validerRekursivt(Set<Class<?>> besøkteKlasser, Class<?> klasse, Class<?> forrigeKlasse) {
        if (besøkteKlasser.contains(klasse)) {
            return;
        }
        besøkteKlasser.add(klasse);

        if (klasse.getAnnotation(Entity.class) != null || klasse.getAnnotation(MappedSuperclass.class) != null) {
            fail("Klassen " + klasse + " er en entitet, kan ikke brukes som DTO. Brukes i " + forrigeKlasse);
        }

        for (var subklasse : JsonSubTypesUtil.getJsonSubtypes(klasse)) {
            validerRekursivt(besøkteKlasser, subklasse, klasse);
        }
        for (var field : getRelevantFields(klasse)) {
            if (field.getAnnotation(JsonIgnore.class) != null) {
                continue; //feltet blir hverken serialisert elle deserialisert, unntas fra sjekk
            }
            if (field.getType().isEnum()) {
                continue; //enum er OK
            }
            if (getVurderingsalternativer(field.getType()) != null) {
                validerRiktigAnnotert(field); //har konfigurert opp spesifikk validering
            } else if (field.getType().getName().startsWith("java")) {
                fail("Feltet " + field + " har ikke påkrevde annoteringer. Trenger evt. utvidelse av denne testen for å akseptere denne typen.");
            } else {
                validerHarValidAnnotering(field);
                validerRekursivt(besøkteKlasser, field.getType(), klasse);
            }
            if (brukerGenerics(field)) {
                validerRekursivt(besøkteKlasser, field.getType(), klasse);
                for (var klazz : genericTypes(field)) {
                    validerRekursivt(besøkteKlasser, klazz, klasse);
                }
            }
        }
    }

    private static void validerHarValidAnnotering(Field field) {
        if (field.getAnnotation(Valid.class) == null) {
            fail("Feltet " + field + " må ha @Valid-annotering");
        }
    }

    private static Set<Class<?>> genericTypes(Field field) {
        Set<Class<?>> klasser = new HashSet<>();
        var type = (ParameterizedType) field.getGenericType();
        for (var t : type.getActualTypeArguments()) {
            klasser.add((Class<?>) t);
        }
        return klasser;
    }

    private static boolean brukerGenerics(Field field) {
        return field.getGenericType() instanceof ParameterizedType;
    }

    private static Set<Field> getRelevantFields(Class<?> klasse) {
        Set<Field> fields = new LinkedHashSet<>();
        while (!klasse.isPrimitive() && !klasse.getName().startsWith("java")) {
            fields.addAll(fjernStaticFields(Arrays.asList(klasse.getDeclaredFields())));
            klasse = klasse.getSuperclass();
        }
        return fields;
    }

    private static Collection<Field> fjernStaticFields(List<Field> fields) {
        return fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
    }

    private static void validerRiktigAnnotert(Field field) {
        var alternativer = getVurderingsalternativer(field.getType());
        for (var alternativ : alternativer) {
            var harAlleAnnoteringerForAlternativet = true;
            for (var annotering : alternativ) {
                if (field.getAnnotation(annotering) == null) {
                    harAlleAnnoteringerForAlternativet = false;
                }
            }
            if (harAlleAnnoteringerForAlternativet) {
                return;
            }
        }
        throw new IllegalArgumentException("Feltet " + field + " har ikke påkrevde annoteringer: " + alternativer);
    }

}
