-- Tabell TILKJENT_YTELSE
CREATE TABLE TILKJENT_YTELSE (
    id                                    number(19, 0) not null,
    behandling_id                         number(19, 0) not null,
    endringsdato                          date,
    er_opphoer                            char(1) not null check (er_opphoer in ('J', 'N')),
    er_opphoer_etter_stp                  char(1) not null check (er_opphoer_etter_stp in ('J', 'N')),
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3)
);

ALTER TABLE TILKJENT_YTELSE ADD CONSTRAINT PK_TILKJENT_YTELSE primary key (id);

CREATE SEQUENCE SEQ_TILKJENT_YTELSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell TY_BEHANDLING_INFO
CREATE TABLE TY_BEHANDLING_INFO (
    id                        number(19, 0) not null,
    tilkjent_ytelse_id        number(19, 0) not null,
    saksnummer                varchar2(20 char) not null,
    forrige_behandling_id     number(19, 0),
    aktoer_id                 varchar2(50 char) not null,
    gjelder_adopsjon          char(1) not null check (gjelder_adopsjon in ('J', 'N')),
    vedtaksdato               date not null,
    ansvarlig_saksbehandler   varchar2(100 char) not null,
    versjon                   number(19, 0) default 0 not null,
    opprettet_av              varchar2(20 char) default 'VL' not null,
    opprettet_tid             timestamp(3) default systimestamp not null,
    endret_av                 varchar2(20 char),
    endret_tid                timestamp(3)
);

ALTER TABLE TY_BEHANDLING_INFO ADD CONSTRAINT PK_TY_BEHANDLING_INFO primary key (id);
ALTER TABLE TY_BEHANDLING_INFO ADD CONSTRAINT FK_TY_BEHANDLING_INFO_1 FOREIGN KEY (TILKJENT_YTELSE_ID) REFERENCES TILKJENT_YTELSE(id);

CREATE INDEX IDX_TY_BEHANDLING_INFO_01 ON TY_BEHANDLING_INFO (tilkjent_ytelse_id);

CREATE SEQUENCE SEQ_TY_BEHANDLING_INFO MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell TILKJENT_YTELSE_PERIODE
CREATE TABLE TILKJENT_YTELSE_PERIODE (
    id                    number(19, 0) not null,
    tilkjent_ytelse_id    number(19, 0) not null,
    fom                   date not null,
    tom                   date not null,
    versjon               number(19, 0) default 0 not null,
    opprettet_av          varchar2(20 char) default 'VL' not null,
    opprettet_tid         timestamp(3) default systimestamp not null,
    endret_av             varchar2(20 char),
    endret_tid            timestamp(3)
);

ALTER TABLE TILKJENT_YTELSE_PERIODE ADD CONSTRAINT PK_TILKJENT_YTELSE_PERIODE primary key (id);
ALTER TABLE TILKJENT_YTELSE_PERIODE ADD CONSTRAINT FK_TILKJENT_YTELSE_PERIODE_1 FOREIGN KEY (TILKJENT_YTELSE_ID) REFERENCES TILKJENT_YTELSE(id);

CREATE INDEX IDX_TILKJENT_YTELSE_PERIODE_01 ON TILKJENT_YTELSE_PERIODE (tilkjent_ytelse_id);

CREATE SEQUENCE SEQ_TILKJENT_YTELSE_PERIODE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell TILKJENT_YTELSE_ANDEL
CREATE TABLE TILKJENT_YTELSE_ANDEL (
    id                                    number(19, 0) not null,
    ty_periode_id                         number(19, 0) not null,
    utbetales_til_bruker                  char(1) not null check (utbetales_til_bruker IN ('J', 'N')),
    arbeidsgiver_org_nr                   varchar2(100 char),
    arbeidsgiver_aktoer_id                varchar2(100 char),
    inntektskategori                      varchar2(100 char) not null,
    utbetalingsgrad                       number(5, 2) not null,
    sats_beloep                           number(19, 0) not null,
    sats_type                             varchar2(20 char) not null,
    kl_inntektskategori                   varchar2(100 char) as ('INNTEKTSKATEGORI'),
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3)
);

ALTER TABLE TILKJENT_YTELSE_ANDEL ADD CONSTRAINT PK_TILKJENT_YTELSE_ANDEL primary key (id);
ALTER TABLE TILKJENT_YTELSE_ANDEL ADD CONSTRAINT FK_TILKJENT_YTELSE_ANDEL_1 FOREIGN KEY (ty_periode_id) REFERENCES TILKJENT_YTELSE_PERIODE(id);
ALTER TABLE TILKJENT_YTELSE_ANDEL ADD CONSTRAINT FK_TILKJENT_YTELSE_ANDEL_2 FOREIGN KEY (kl_inntektskategori, inntektskategori) REFERENCES KODELISTE(kodeverk, kode);

CREATE INDEX IDX_TILKJENT_YTELSE_ANDEL_01 ON TILKJENT_YTELSE_ANDEL (ty_periode_id);
CREATE INDEX IDX_TILKJENT_YTELSE_ANDEL_04 ON TILKJENT_YTELSE_ANDEL (inntektskategori);

CREATE SEQUENCE SEQ_TILKJENT_YTELSE_ANDEL MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell TILKJENT_YTELSE_FERIEPENGER
CREATE TABLE TY_FERIEPENGER (
    id                                    number(19, 0) not null,
    ty_andel_id                           number(19, 0) not null,
    opptjeningsaar                        number(19, 0) not null,
    aarsbeloep                            number(19, 0) not null,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3)
);

ALTER TABLE TY_FERIEPENGER ADD CONSTRAINT PK_TY_FERIEPENGER primary key (id);
ALTER TABLE TY_FERIEPENGER ADD CONSTRAINT FK_TY_FERIEPENGER_1 FOREIGN KEY (ty_andel_id) REFERENCES TILKJENT_YTELSE_ANDEL(id);

CREATE INDEX IDX_TY_FERIEPENGER_01 ON TY_FERIEPENGER (ty_andel_id);

CREATE SEQUENCE SEQ_TY_FERIEPENGER MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- TILKJENT_YTELSE
COMMENT ON TABLE TILKJENT_YTELSE IS 'Aggregat for tilkjent ytelse';
COMMENT ON COLUMN TILKJENT_YTELSE.ID IS 'Primary key';
COMMENT ON COLUMN TILKJENT_YTELSE.BEHANDLING_ID IS 'Id av behandling som er knyttet til tilkjent ytelse';
COMMENT ON COLUMN TILKJENT_YTELSE.ER_OPPHOER IS 'Om behandling resultat er opphør';
COMMENT ON COLUMN TILKJENT_YTELSE.ER_OPPHOER_ETTER_STP IS 'Om opphør fom dato er etter skjæringstidspunkt';
COMMENT ON COLUMN TILKJENT_YTELSE.ENDRINGSDATO IS 'Endringsdato for tilkjent ytelse';

-- TY_BEHANDLING_INFO
COMMENT ON TABLE TY_BEHANDLING_INFO IS 'Info om behandling som er knyttet til tilkjent ytelse';
COMMENT ON COLUMN TY_BEHANDLING_INFO.ID IS 'Primary key';
COMMENT ON COLUMN TY_BEHANDLING_INFO.TILKJENT_YTELSE_ID IS 'FK: TILKJENT_YTELSE';
COMMENT ON COLUMN TY_BEHANDLING_INFO.SAKSNUMMER IS 'Saksnummer i GSAK';
COMMENT ON COLUMN TY_BEHANDLING_INFO.FORRIGE_BEHANDLING_ID IS 'Id av forrige behandling';
COMMENT ON COLUMN TY_BEHANDLING_INFO.AKTOER_ID IS 'AktørId til bruker';
COMMENT ON COLUMN TY_BEHANDLING_INFO.GJELDER_ADOPSJON IS 'Om behandling tema gjelder adopsjon';
COMMENT ON COLUMN TY_BEHANDLING_INFO.VEDTAKSDATO IS 'Vedtaksdato for behandling';
COMMENT ON COLUMN TY_BEHANDLING_INFO.ANSVARLIG_SAKSBEHANDLER IS 'Ansvarlig saksbehandler som godkjente vedtaket';

-- TILKJENT_YTELSE_PERIODE
COMMENT ON TABLE TILKJENT_YTELSE_PERIODE IS 'Tilkjent ytelse perioder';
COMMENT ON COLUMN TILKJENT_YTELSE_PERIODE.ID IS 'Primary key';
COMMENT ON COLUMN TILKJENT_YTELSE_PERIODE.TILKJENT_YTELSE_ID IS 'FK: TILKJENT_YTELSE';
COMMENT ON COLUMN TILKJENT_YTELSE_PERIODE.FOM IS 'Første dag i periode for tilkjent ytelse';
COMMENT ON COLUMN TILKJENT_YTELSE_PERIODE.TOM IS 'Siste dag i periode for tilkjent ytelse';

-- TILKJENT_YTELSE_ANDEL
COMMENT ON TABLE TILKJENT_YTELSE_ANDEL IS 'Andel i tilkjent ytelse';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.ID IS 'Primary key';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.TY_PERIODE_ID IS 'FK: TILKJENT_YTELSE_PERIODE';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.UTBETALES_TIL_BRUKER IS 'Angir om bruker eller arbeidsgiver er mottaker';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.ARBEIDSGIVER_ORG_NR IS 'Arbeidsforhold orgnr denne andelen er knyttet til';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.ARBEIDSGIVER_AKTOER_ID IS 'Arbeidsforhold aktørId denne andelen er knyttet til';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.INNTEKTSKATEGORI IS 'Inntektskategori for andelen';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.KL_INNTEKTSKATEGORI IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.UTBETALINGSGRAD IS 'Uttaksgrad';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.SATS_BELOEP IS 'Sats beløp for tilkjent ytelse';
COMMENT ON COLUMN TILKJENT_YTELSE_ANDEL.SATS_TYPE IS 'Type av sats - eks. dagsats';

-- TILKJENT_YTELSE_FERIEPENGER
COMMENT ON TABLE TY_FERIEPENGER IS 'Årsverdier av feriepenger knyttet til andel';
COMMENT ON COLUMN TY_FERIEPENGER.ID IS 'Primary key';
COMMENT ON COLUMN TY_FERIEPENGER.TY_ANDEL_ID IS 'FK: TILKJENT_YTELSE_ANDEL';
COMMENT ON COLUMN TY_FERIEPENGER.OPPTJENINGSAAR IS '31/12 i opptjeningsåret, dvs året før feriepengene utbetales';
COMMENT ON COLUMN TY_FERIEPENGER.AARSBELOEP IS 'Årsbeløp som skal utbetales, avrundet';
