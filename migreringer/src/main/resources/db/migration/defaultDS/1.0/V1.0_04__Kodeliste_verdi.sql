INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES
  (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'BOOLEAN', NULL, 'Boolske verdier', 'Støtter J(a) / N(ei) flagg', 'NB',
                          to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'PERIOD', NULL, 'Periode verdier',
                               'ISO 8601 Periode verdier.  Eks. P10M (10 måneder), P1D (1 dag) ', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'DURATION', NULL, 'Periode verdier',
                               'ISO 8601 Duration (tid) verdier.  Eks. PT1H (1 time), PT1M (1 minutt) ', 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES
  (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'INTEGER', NULL, 'Heltall', 'Heltallsverdier (positiv/negativ)', 'NB',
                          to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'STRING', NULL, 'Streng verdier', NULL, 'NB',
                               to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_TYPE', 'URI', NULL, 'Uniform Resource Identifier',
                               'URI for å angi id til en ressurs', 'NB', to_date('01.01.2000', 'DD.MM.RRRR'),
                               to_date('31.12.9999', 'DD.MM.RRRR'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'KONFIG_VERDI_GRUPPE', 'INGEN', NULL, '-',
                               'Ingen gruppe definert (default).  Brukes istdf. NULL siden dette inngår i en Primary Key. Koder som ikke er del av en gruppe må alltid være unike.',
                               'NB', to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));