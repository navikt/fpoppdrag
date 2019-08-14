package no.nav.foreldrepenger.oppdrag.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class XmlStringFieldFikser {

    private static final Set<Class> KLASSER_SOM_IKKE_ITERERES_OVER = new HashSet<>();

    static {
        KLASSER_SOM_IKKE_ITERERES_OVER.add(Boolean.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(boolean.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(BigDecimal.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(BigInteger.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(Long.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(long.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(Integer.class);
        KLASSER_SOM_IKKE_ITERERES_OVER.add(int.class);
    }

    private XmlStringFieldFikser() {
        //hindrer instansiering - så blir SonarQube glad
    }

    /**
     * Itererer over objektet med reflection, og stripper vekk mellomrom etter verdien
     *
     * @param obj
     */
    public static void stripTrailingSpacesFromStrings(Object obj) {
        if (Objects.nonNull(obj)) {
            Field[] fields = obj.getClass().getDeclaredFields();
            try {
                for (Field f : fields) {
                    f.setAccessible(true);
                    if (f.getType() == String.class) {
                        f.set(obj, ((String) f.get(obj)).trim());
                        String s = (String) f.get(obj);
                        if (s != null && s.length() == 0) {
                            f.set(obj, null);
                        }
                    } else if (f.getType() == List.class) {
                        List<Object> objList = (List<Object>) f.get(obj);
                        for (Object o : objList) {
                            stripTrailingSpacesFromStrings(o);
                        }
                    } else if (!KLASSER_SOM_IKKE_ITERERES_OVER.contains(f.getType())) {
                        stripTrailingSpacesFromStrings(f.get(obj));
                    }
                }
            } catch (IllegalAccessException e) {
                // field settes til accessible så IllegalAccessException skal ikke oppstå
                throw new IllegalStateException(e);
            }
        }
    }

}
