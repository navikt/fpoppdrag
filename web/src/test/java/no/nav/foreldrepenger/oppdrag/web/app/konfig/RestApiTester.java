package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

public class RestApiTester {


    static final List<Class<?>> UNNTATT = List.of();

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        return new ArrayList<>(finnAlleRestTjenester(new ApiConfig()));
    }

    static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses().stream()
                .filter(c -> c.getAnnotation(Path.class) != null)
                .filter(c -> !UNNTATT.contains(c))
                .toList();
    }
}
