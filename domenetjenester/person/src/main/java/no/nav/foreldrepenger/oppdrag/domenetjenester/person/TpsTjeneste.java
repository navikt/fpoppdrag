package no.nav.foreldrepenger.oppdrag.domenetjenester.person;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;

@ApplicationScoped
public class TpsTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(TpsTjeneste.class);

    private AktørConsumerMedCache aktørConsumer;
    private PdlKlient pdlKlient;

    public TpsTjeneste() {
    }

    @Inject
    public TpsTjeneste(AktørConsumerMedCache aktørConsumer,
                       PdlKlient pdlKlient) {
        this.aktørConsumer = aktørConsumer;
        this.pdlKlient = pdlKlient;
    }

    public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent) {
        var aktørId = aktørConsumer.hentAktørIdForPersonIdent(personIdent.getIdent()).map(AktørId::new);
        aktørId.ifPresent(a -> hentAktørIdFraPDL(personIdent.getIdent(), a.getId()));
        return aktørId;
    }

    public Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId) {
        var ident = aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new);
        ident.ifPresent(i -> hentPersonIdentFraPDL(aktørId.getId(), i.getIdent()));
        return ident;
    }

    public void hentAktørIdFraPDL(String fnr, String aktørFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(fnr);
            request.setGrupper(List.of(IdentGruppe.AKTORID));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: like aktørid");
            } else if (antall != 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall aktørid {}", antall);
            } else {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulike aktørid TPS {} og PDL {} antall {}", aktørFraConsumer, aktørId, antall);
            }
        } catch (Exception e) {
            LOG.info("FPOPPDRAG PDL AKTØRID hentaktørid error", e);
        }
    }

    public void hentPersonIdentFraPDL(String aktørId, String identFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(aktørId);
            request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var fnr = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: like identer");
            } else if (antall != 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall identer {}", antall);
            } else {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulike identer TPS {} og PDL {} antall {}", identFraConsumer, fnr, antall);
            }
        } catch (Exception e) {
            LOG.info("FPOPPDRAG PDL AKTØRID hentident error", e);
        }
    }

}
