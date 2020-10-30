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
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

@ApplicationScoped
public class TpsTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(TpsTjeneste.class);

    private PdlKlient pdlKlient;
    private AktørConsumerMedCache aktørConsumer;
    private PersonConsumer personConsumer;


    public TpsTjeneste() {
        // for CDI proxy
    }

    @Inject
    public TpsTjeneste(PdlKlient pdlKlient,
                       AktørConsumerMedCache aktørConsumer,
                       PersonConsumer personConsumer) {
        this.pdlKlient = pdlKlient;
        this.aktørConsumer = aktørConsumer;
        this.personConsumer = personConsumer;
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        var aid = aktørConsumer.hentAktørIdForPersonIdent(fnr.getIdent()).map(AktørId::new);
        aid.ifPresent(a -> hentAktørIdFraPDL(fnr, a.getId()));
        return aid;
    }

    public Optional<PersonIdent> hentFnr(AktørId aktørId) {
        var ident = aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new);
        ident.ifPresent(i -> hentPersonIdentFraPDL(aktørId,i.getIdent()));
        return ident;
    }

    public Personinfo hentPersoninfoFor(PersonIdent ident) {
        return hentPersoninfo(ident);
    }

    private Personinfo hentPersoninfo(PersonIdent fnr) {
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(TpsUtil.lagPersonIdent(fnr.getIdent()));
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            Person person = response.getPerson();
            if (!(person instanceof Bruker)) {
                throw TpsFeilmeldinger.FACTORY.ukjentBrukerType().toException();
            }
            Bruker bruker = (Bruker) person;
            var pi = new Personinfo(new PersonIdent(fnr.getIdent()), bruker.getPersonnavn().getSammensattNavn());
            hentNavnFraPdl(fnr.getIdent(), pi);
            return pi;
        } catch (HentPersonPersonIkkeFunnet e) {
            throw TpsFeilmeldinger.FACTORY.fantIkkePerson(e).toException();
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw TpsFeilmeldinger.FACTORY.tpsUtilgjengeligSikkerhetsbegrensning(e).toException();
        }
    }

    private void hentAktørIdFraPDL(PersonIdent fnr, String aktørFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(fnr.getIdent());
            request.setGrupper(List.of(IdentGruppe.AKTORID));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident().gruppe());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: like aktørid");
            } else if (antall != 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall aktørid {}", antall);
            } else {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulike aktørid TPS og PDL, antall {}", antall);
            }
        } catch (Exception e) {
            LOG.info("FPOPPDRAG PDL AKTØRID hentaktørid error", e);
        }
    }

    private void hentPersonIdentFraPDL(AktørId aktørId, String identFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(aktørId.getId());
            request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident().gruppe());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var fnr = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: like identer");
            } else if (antall != 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall identer {}", antall);
            } else {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulike identer TPS og PDL antall {}", antall);
            }
        } catch (Exception e) {
            LOG.info("FPOPPDRAG PDL AKTØRID hentident error", e);
        }
    }

    private void hentNavnFraPdl(String ident, Personinfo fraTps) {
        try {
            var request = new HentPersonQueryRequest();
            request.setIdent(ident);
            var projection = new PersonResponseProjection()
                    .navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn());

            var person = pdlKlient.hentPerson(request, projection, Tema.FOR);

            var navn = person.getNavn().stream().map(TpsTjeneste::mapNavn).findFirst().orElseThrow();

            if (Objects.equals(fraTps.getNavn(), navn)) {
                LOG.info("FPOPPDRAG PDL AKTØRID: like navn");
            } else {
                LOG.info("FPOPPDRAG PDL AKTØRID: ulike navn TPS og PDL");
            }
        } catch (Exception e) {
            LOG.info("FPOPPDRAG PDL AKTØRID hentident error", e);
        }
    }

    private static String mapNavn(Navn navn) {
        if (navn.getForkortetNavn() != null)
            return navn.getForkortetNavn();
        return navn.getEtternavn() + " " + navn.getFornavn() + (navn.getMellomnavn() == null ? "" : " " + navn.getMellomnavn());
    }
}
