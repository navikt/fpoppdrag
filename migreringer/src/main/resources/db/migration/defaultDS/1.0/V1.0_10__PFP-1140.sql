Delete from KODELISTE where kode='PP' and kodeverk = 'FAG_OMRAADE_KODE';

UPDATE KODELISTE set navn = beskrivelse where kodeverk = 'FAG_OMRAADE_KODE';
UPDATE KODELISTE set navn = beskrivelse where kodeverk = 'POSTERING_TYPE';

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'FAG_OMRAADE_KODE','FPREF',null, 'Foreldrepenger refusjon', 'Foreldrepenger refusjon til arbeidsgiver',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'FAG_OMRAADE_KODE','SPREF',null, 'Sykepenger refusjon', 'Sykepenger refusjon til arbeidsgiver',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'FAG_OMRAADE_KODE','OOP',null, 'Omsorg, opplæring, og pleiepenger', 'Omsorg, opplæring, og pleiepenger',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'FAG_OMRAADE_KODE','OOPREF',null, 'Omsorg, opplæring, og pleiepenger refusjon', 'Omsorg, opplæring, og pleiepenger refusjon til arbeidsgiver',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
values (seq_kodeliste.nextval,'FAG_OMRAADE_KODE','REFUTG',null, 'Engangsstønad', 'Engangsstønad til bruker',
        to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');