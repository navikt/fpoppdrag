package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class TestResourceLoader {

    public static String loadXml(String resource) throws Exception {
        final InputStream inputStream = TestResourceLoader.class.getResourceAsStream(resource);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(
                inputStream, Charset.forName("UTF-8")))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

}
