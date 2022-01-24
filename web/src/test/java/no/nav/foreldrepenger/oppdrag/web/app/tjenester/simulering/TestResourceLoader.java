package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Base64;

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

    public static String loadB64Xml(String resource) throws Exception {
        String xml = loadXml(resource);
        return Base64.getEncoder().encodeToString(xml.getBytes(Charset.forName("UTF-8")));
    }

}
