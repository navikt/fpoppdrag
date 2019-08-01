INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('KONFIG_VERDI_GRUPPE', 'VL', NULL, NULL, NULL, 'N', 'N', 'KonfigVerdiGruppe',
                               'Angir en gruppe konfigurerbare verdier tilhører. Det åpner for å kunne ha lister og Maps av konfigurerbare verdier',
                               'VL', to_timestamp('04.12.2017', 'DD.MM.RRRR'), 'N');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, SAMMENSATT)
VALUES ('KONFIG_VERDI_TYPE', 'VL', NULL, NULL, NULL, 'N', 'N', 'KonfigVerdiType',
                             'Angir type den konfigurerbare verdien er av slik at dette kan brukes til validering og fremstilling.',
                             'VL', to_timestamp('04.12.2017', 'DD.MM.RRRR'), 'N');