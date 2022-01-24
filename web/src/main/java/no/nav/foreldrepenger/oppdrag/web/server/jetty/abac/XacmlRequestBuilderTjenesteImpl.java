package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;

import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@Dependent
@Alternative
@Priority(2)
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    XacmlRequestBuilderTjenesteImpl() {
        //for CDI
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, pdpRequest.getString(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        int antall = antallResources(pdpRequest);
        for (int i = 0; i < antall; i++) {
            XacmlAttributeSet resourceAttributeSet = byggXacmlResourceAttrSet(pdpRequest, i);
            xacmlBuilder.addResourceAttributeSet(resourceAttributeSet);
        }
        return xacmlBuilder;
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest));
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
    }

    private XacmlAttributeSet byggXacmlResourceAttrSet(PdpRequest pdpRequest, int index) {
        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        int antallFnrPåRequest = pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
        if (index < antallFnrPåRequest) {
            setOptionalListValueinAttributeSet(resourceAttributeSet, pdpRequest, RESOURCE_FELLES_PERSON_FNR, index);
        } else {
            int kalkulertIndex = index - antallFnrPåRequest;
            setOptionalListValueinAttributeSet(resourceAttributeSet, pdpRequest, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, kalkulertIndex);
        }

        return resourceAttributeSet;
    }

    private void setOptionalListValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key, int index) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            Optional.ofNullable(list.get(index)).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
        }
    }
}
