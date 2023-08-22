package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class SimuleringDetaljerDto {

    @NotNull
    private LocalDate fom;

    @NotNull
    private LocalDate tom;

    @NotNull
    @Size(max = 100, message = "fagområdeKode er for høyt")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fagomraadeKode;

    @NotNull
    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @DecimalMin("-999999999.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 9, fraction = 2, message = "Beløpet er for høyt")
    private BigDecimal beløp;

    @NotNull
    @Size(max = 1, message = "betalingType er for høyt")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String betalingType;

    @NotNull
    @Size(max = 100, message = "posteringType er for høyt")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String posteringType;

    @NotNull
    private LocalDate forfallsdato;

    private boolean utenInntrekk;

    private SimuleringDetaljerDto() {
        // CDI
    }

    public SimuleringDetaljerDto(LocalDate fom, LocalDate tom,
                                 String fagomraadeKode,
                                 BigDecimal beløp, String betalingType,
                                 String posteringType,
                                 LocalDate forfallsdato, boolean utenInntrekk) {
        this.fom = fom;
        this.tom = tom;
        this.fagomraadeKode = fagomraadeKode;
        this.beløp = beløp;
        this.betalingType = betalingType;
        this.posteringType = posteringType;
        this.forfallsdato = forfallsdato;
        this.utenInntrekk = utenInntrekk;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getFagomraadeKode() {
        return fagomraadeKode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public String getBetalingType() {
        return betalingType;
    }

    public String getPosteringType() {
        return posteringType;
    }

    public LocalDate getForfallsdato() {
        return forfallsdato;
    }

    public boolean isUtenInntrekk() {
        return utenInntrekk;
    }
}

