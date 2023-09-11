package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.YtelseUtleder;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.PeriodeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;

public class SimuleringForMottakerDtoBuilder {

    private static final Map<Fagområde, Integer> STANDARD_SORT = initStandardMap();
    private YtelseType gjelderYtelsetype;

    private MottakerType mottakerType;
    private String mottakerNummer;
    private String mottakerIdentifikator;
    private List<SimuleringDto.SimuleringResultatPerFagområdeDto> resultatPerFagområde = new ArrayList<>();
    private List<SimuleringDto.SimuleringResultatRadDto> resultatOgMotregningRader = new ArrayList<>();
    private PeriodeDto nesteUtbPeriode;

    public SimuleringForMottakerDtoBuilder medResultatPerFagområde(List<SimuleringDto.SimuleringResultatPerFagområdeDto> resultatPerFagområde) {
        this.resultatPerFagområde = resultatPerFagområde;
        return this;
    }

    public SimuleringForMottakerDtoBuilder medResultatOgMotregningRader(List<SimuleringDto.SimuleringResultatRadDto> resultatOgMotregningRader) {
        this.resultatOgMotregningRader = resultatOgMotregningRader;
        return this;
    }

    public SimuleringForMottakerDtoBuilder medMottakerType(MottakerType mottakerType) {
        this.mottakerType = mottakerType;
        return this;
    }

    public SimuleringForMottakerDtoBuilder medMottakerIdentifikator(String mottakerIdentifikator) {
        this.mottakerIdentifikator = mottakerIdentifikator;
        return this;
    }

    public SimuleringForMottakerDtoBuilder medMottakerNummer(String mottakerNummer) {
        this.mottakerNummer = mottakerNummer;
        return this;
    }

    public SimuleringForMottakerDtoBuilder medNesteUtbetalingsperiode(LocalDate fom, LocalDate tom) {
        this.nesteUtbPeriode = new PeriodeDto(fom, tom);
        return this;
    }

    public SimuleringDto.SimuleringForMottakerDto build() {
        if (this.nesteUtbPeriode == null) {
            this.nesteUtbPeriode = new PeriodeDto(null, null);
        }
        var sortering = sorteringForYtelsetype(gjelderYtelsetype);
        this.resultatPerFagområde.sort(Comparator.comparingInt(o -> sortering.getOrDefault(o.fagOmrådeKode(), Integer.MAX_VALUE)));
        this.resultatOgMotregningRader.sort(Comparator.comparingInt(o -> o.feltnavn().ordinal()));
        return new SimuleringDto.SimuleringForMottakerDto(this.mottakerType, this.mottakerNummer, this.mottakerIdentifikator, this.resultatPerFagområde,
            this.resultatOgMotregningRader, this.nesteUtbPeriode);
    }

    private EnumMap<Fagområde, Integer> sorteringForYtelsetype(YtelseType gjelderYtelsetype) {
        var sortering = new EnumMap<>(STANDARD_SORT);

        //flytter gjeldende ytelsetype først i sorteringen
        sortering.forEach((fagområde, value) -> {
            if (YtelseUtleder.utledFor(fagområde).equals(gjelderYtelsetype)) {
                sortering.put(fagområde, sortering.get(fagområde) - 100);
            }
        });
        return sortering;
    }

    private static EnumMap<Fagområde, Integer> initStandardMap() {
        return new EnumMap<>(Map.ofEntries(Map.entry(Fagområde.REFUTG, 101), Map.entry(Fagområde.SVP, 101), Map.entry(Fagområde.SVPREF, 102),
            Map.entry(Fagområde.FP, 103), Map.entry(Fagområde.FPREF, 104), Map.entry(Fagområde.SP, 105), Map.entry(Fagområde.SPREF, 106),
            Map.entry(Fagområde.OOP, 107), Map.entry(Fagområde.OOPREF, 108), Map.entry(Fagområde.PB, 109), Map.entry(Fagområde.PBREF, 110),
            Map.entry(Fagområde.PN, 111), Map.entry(Fagområde.PNREF, 112), Map.entry(Fagområde.OM, 113), Map.entry(Fagområde.OMREF, 114),
            Map.entry(Fagområde.OPP, 115), Map.entry(Fagområde.OPPREF, 116)));
    }

    public SimuleringForMottakerDtoBuilder medGjelderYtelseType(YtelseType gjelderYtelseType) {
        this.gjelderYtelsetype = gjelderYtelseType;
        return this;
    }
}
