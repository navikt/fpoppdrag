INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'FAG_OMRAADE_KODE', 'SVP', 'Svangerskapspenger', 'Svangerskapspenger', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'FAG_OMRAADE_KODE', 'SVPREF', 'Svangerskapspenger refusjon', 'Svangerskapspenger refusjon til arbeidsgiver', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');


Insert into KODEVERK (KODE,KODEVERK_EIER,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,SAMMENSATT)
values ('YTELSE_TYPE','VL','N','N','YtelseType','Type ytelse.','N');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'YTELSE_TYPE', 'ES', 'Engangsstønad', 'Engangsstønad', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'YTELSE_TYPE', 'FP', 'Foreldrepenger', 'Foreldrepenger', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'YTELSE_TYPE', 'SVP', 'Svangerskapspenger', 'Svangerskapspenger', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, SPRAK)
VALUES (seq_kodeliste.nextval, 'YTELSE_TYPE', '-', 'Udefinert', 'Udefinert', to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'), 'NB');


