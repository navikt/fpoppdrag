package no.nav.foreldrepenger.oppdrag.web.app.konfig;


import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;


public class InternalApplicationTest {

    @Test
    public void getClasses() {
        InternalApplication app = new InternalApplication();
        Set<Class<?>> classes = app.getClasses();
        assertEquals(classes.size(), 2);
    }
}
