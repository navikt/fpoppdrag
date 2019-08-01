-- ny kodeverk INNTEKTSKATEGORI
insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_EIER_REF,KODEVERK_EIER_VER,KODEVERK_EIER_NAVN,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT)
values ('INNTEKTSKATEGORI','VL',null,null,'Inntektskategori','N','N','Inntektskategori','Inntektskategori som er definert i fpsak','N');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','ARBEIDSTAKER', null, 'Arbeidstaker', 'Arbeidstaker', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','FRILANSER', null, 'Frilanser', 'Frilanser', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','SELVSTENDIG_NÆRINGSDRIVENDE', null, 'Selvstendig næringsdrivende (ordinær næring)', 'Selvstendig næringsdrivende (ordinær næring)',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','DAGPENGER', null, 'Dagpenger', 'Dagpenger', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','ARBEIDSAVKLARINGSPENGER', null, 'Arbeidsavklaringspenger', 'Arbeidsavklaringspenger',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','SJØMANN', null, 'Sjømann', 'Sjømann', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','DAGMAMMA', null, 'Selvstendig næringsdrivende (dagmamma)', 'Selvstendig næringsdrivende (dagmamma)',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','JORDBRUKER', null, 'Selvstendig næringsdrivende (jordbruker)', 'Selvstendig næringsdrivende (jordbruker)',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','FISKER', null, 'Selvstendig næringsdrivende (fisker)', 'Selvstendig næringsdrivende (fisker)',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','ARBEIDSTAKER_UTEN_FERIEPENGER', null, 'Arbeidstaker uten feriepenger', 'Arbeidstaker uten feriepenger',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'INNTEKTSKATEGORI','-', null, 'Ingen inntektskategori (default)', 'Ingen inntektskategori (default)',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');