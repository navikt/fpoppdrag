package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.organisasjon.AbstractOrganisasjonKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "organisasjon.rs.url", endpointDefault = "https://modapp.adeo.no/ereg/api/v1/organisasjon")
public class OrganisasjonKlient extends AbstractOrganisasjonKlient {

    public OrganisasjonKlient() {
        super();
    }
}
