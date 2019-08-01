package no.nav.foreldrepenger.oppdrag.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import no.nav.vedtak.feil.Feil;

public class ConstraintViolationMapperTest {

    @Test
    public void toResponse() {
        String message = "message";
        String template = "template";
        String rootBean = "rootBean";
        Class<String> rootBeanClass = null;
        Object leafBean = "leafBean";
        Object [] execParam = {"param1"};
        Object returnValue = "returnValue";
        Path path = new Path() {
            @Override
            public Iterator<Node> iterator() {
                return null;
            }
        };
        Object invalid = null;
        TestViolation violation = new TestViolation(message, template, rootBean, rootBeanClass, leafBean, execParam, returnValue, path, invalid);
        Set<TestViolation> violationList = new HashSet<>();
        violationList.add(violation);
        ConstraintViolationMapper mapper = new ConstraintViolationMapper();
        ConstraintViolationException exception = new ConstraintViolationException(violationList);
        Response response = mapper.toResponse(exception);

        Collection<FeltFeilDto> feilene = new ArrayList<>();
        List<String> feltNavn = new ArrayList<>();
        feltNavn.add("null");
        Feil feil = FeltValideringFeil.FACTORY.feltverdiKanIkkeValideres(feltNavn);
        FeilDto feilDto = new FeilDto(feil.getFeilmelding(), feilene);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        FeilDto dto = (FeilDto) response.getEntity();
        assertThat(dto.getFeilmelding()).isEqualTo(feilDto.getFeilmelding());
        assertThat(response.getMediaType().toString()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    class TestViolation implements ConstraintViolation<String> {

        private String message;
        private String template;
        private String rootBean;
        private Class<String> rootBeanClass;
        private Object leafBean;
        private Object [] execParam;
        private Object returnValue;
        private Path path;
        private Object invalid;

        public TestViolation(String message, String template, String rootBean, Class<String> rootBeanClass, Object leafBean, Object[] execParam, Object returnValue, Path path, Object invalid) {
            this.message = message;
            this.template = template;
            this.rootBean = rootBean;
            this.rootBeanClass = rootBeanClass;
            this.execParam = execParam;
            this.returnValue = returnValue;
            this.path = path;
            this.invalid = invalid;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return template;
        }

        @Override
        public String getRootBean() {
            return rootBean;
        }

        @Override
        public Class<String> getRootBeanClass() {
            return rootBeanClass;
        }

        @Override
        public Object getLeafBean() {
            return leafBean;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return returnValue;
        }

        @Override
        public Path getPropertyPath() {
            return path;
        }

        @Override
        public Object getInvalidValue() {
            return invalid;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> aClass) {
            return null;
        }
    }


}
