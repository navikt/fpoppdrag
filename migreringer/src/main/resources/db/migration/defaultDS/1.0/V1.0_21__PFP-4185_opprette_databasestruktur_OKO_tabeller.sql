-- Tabell OPPDRAG_KONTROLL
CREATE TABLE OPPDRAG_KONTROLL (
    id                    NUMBER(19, 0) NOT NULL,
    behandling_id         NUMBER(19, 0) NOT NULL,
    saksnummer            NUMBER(19, 0) NOT NULL,
    venter_kvittering     VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
    prosess_task_id       NUMBER(19, 0) NOT NULL,
    versjon               NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av          VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid         TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av             VARCHAR2(20 CHAR),
    endret_tid            TIMESTAMP(3),
    CONSTRAINT PK_OPPDRAG_KONTROLL PRIMARY KEY (id)
);

ALTER TABLE OPPDRAG_KONTROLL ADD CONSTRAINT CHK_OPPDRAG_KONTROLL CHECK (VENTER_KVITTERING IN ('J', 'N'));

CREATE INDEX IDX_OPPDRAG_KONTROLL_1 ON OPPDRAG_KONTROLL(behandling_id);
CREATE INDEX IDX_OPPDRAG_KONTROLL_2 ON OPPDRAG_KONTROLL(saksnummer);
CREATE INDEX IDX_OPPDRAG_KONTROLL_3 ON OPPDRAG_KONTROLL(venter_kvittering);
CREATE INDEX IDX_OPPDRAG_KONTROLL_4 ON OPPDRAG_KONTROLL(prosess_task_id);

CREATE SEQUENCE SEQ_OPPDRAG_KONTROLL MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_AVSTEMMING_115
CREATE TABLE OKO_AVSTEMMING_115 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  kode_komponent            VARCHAR2(10 CHAR) NOT NULL,
  noekkel_avstemming        VARCHAR2(30 CHAR) NOT NULL,
  tidspunkt_melding         VARCHAR2(30 CHAR) NOT NULL,
  oppdrag110_id             NUMBER(19),
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_AVSTEMMING_115 PRIMARY KEY ( id )
);

ALTER TABLE OKO_AVSTEMMING_115 ADD CONSTRAINT CHK_OKO_AVSTEMMING_115 CHECK (KODE_KOMPONENT IN ('VLFP', 'OS'));

CREATE INDEX IDX_OKO_AVSTEMMING_115_1 ON OKO_AVSTEMMING_115(kode_komponent);
CREATE INDEX IDX_OKO_AVSTEMMING_115_2 ON OKO_AVSTEMMING_115(noekkel_avstemming);
CREATE INDEX IDX_OKO_AVSTEMMING_115_3 ON OKO_AVSTEMMING_115(tidspunkt_melding);

CREATE SEQUENCE SEQ_OKO_AVSTEMMING_115 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_OPPDRAG_110
CREATE TABLE OKO_OPPDRAG_110 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  kode_aksjon               VARCHAR2(7 CHAR) NOT NULL,
  kode_endring              VARCHAR2(10 CHAR) NOT NULL,
  kode_fagomraade           VARCHAR2(10 CHAR) NOT NULL,
  fagsystem_id              NUMBER(19) NOT NULL,
  utbet_frekvens            VARCHAR2(10 CHAR) NOT NULL,
  oppdrag_gjelder_id        VARCHAR2(20 CHAR) NOT NULL,
  dato_oppdrag_gjelder_fom  DATE NOT NULL,
  saksbeh_id                VARCHAR2(10 CHAR) NOT NULL,
  oppdrag_kontroll_id       NUMBER(19) NOT NULL,
  avstemming115_id          NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_OPPDRAG_110 PRIMARY KEY ( id )
);

ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT FK_OKO_OPPDRAG_110_1 FOREIGN KEY ( oppdrag_kontroll_id ) REFERENCES OPPDRAG_KONTROLL ( id );
ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT FK_OKO_OPPDRAG_110_2 FOREIGN KEY ( avstemming115_id ) REFERENCES OKO_AVSTEMMING_115 ( id );

ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT CHK_OKO_OPPDRAG_110_1 CHECK (KODE_AKSJON IN ('A', 'B', 'C', '1', '2', '3', '4', '5', '6', '7'));
ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT CHK_OKO_OPPDRAG_110_2 CHECK (KODE_ENDRING IN ('NY', 'ENDR', 'UEND'));
ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT CHK_OKO_OPPDRAG_110_3 CHECK (KODE_FAGOMRAADE IN ('REFUTG', 'FP', 'FPREF'));
ALTER TABLE OKO_OPPDRAG_110 ADD CONSTRAINT CHK_OKO_OPPDRAG_110_4 CHECK (UTBET_FREKVENS IN ('DAG', 'UKE', 'MND', '14DG', 'ENG'));

CREATE INDEX IDX_OKO_OPPDRAG_110_1 ON OKO_OPPDRAG_110(oppdrag_kontroll_id);
CREATE INDEX IDX_OKO_OPPDRAG_110_2 ON OKO_OPPDRAG_110(fagsystem_id);
CREATE INDEX IDX_OKO_OPPDRAG_110_3 ON OKO_OPPDRAG_110(oppdrag_gjelder_id);
CREATE INDEX IDX_OKO_OPPDRAG_110_4 ON OKO_OPPDRAG_110(avstemming115_id);

CREATE SEQUENCE SEQ_OKO_OPPDRAG_110 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_OPPDRAG_ENHET_120
CREATE TABLE OKO_OPPDRAG_ENHET_120 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  type_enhet                VARCHAR2(20 CHAR) NOT NULL,
  enhet                     VARCHAR2(7 CHAR) NOT NULL,
  dato_enhet_fom            DATE NOT NULL,
  oppdrag110_id             NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_OPPDRAG_ENHET_120 PRIMARY KEY ( id )
);

ALTER TABLE OKO_OPPDRAG_ENHET_120 ADD CONSTRAINT FK_OKO_OPPDRAG_ENHET_120_1 FOREIGN KEY ( oppdrag110_id ) REFERENCES OKO_OPPDRAG_110 ( id );

CREATE INDEX IDX_OKO_OPPDRAG_ENHET_120_1 ON OKO_OPPDRAG_ENHET_120(type_enhet);
CREATE INDEX IDX_OKO_OPPDRAG_ENHET_120_2 ON OKO_OPPDRAG_ENHET_120(enhet);
CREATE INDEX IDX_OKO_OPPDRAG_ENHET_120_3 ON OKO_OPPDRAG_ENHET_120(oppdrag110_id);

CREATE SEQUENCE SEQ_OKO_OPPDRAG_ENHET_120 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_OPPDRAG_LINJE_150
CREATE TABLE OKO_OPPDRAG_LINJE_150 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  kode_endring_linje        VARCHAR2(10 CHAR) NOT NULL,
  kode_status_linje         VARCHAR2(10 CHAR),
  dato_status_fom           DATE,
  vedtaksdato               DATE,
  delytelse_id              NUMBER(19) NOT NULL,
  dato_vedtak_fom           DATE NOT NULL,
  dato_vedtak_tom           DATE,
  kode_klassifik            VARCHAR2(50 CHAR) NOT NULL,
  sats                      NUMBER(19) NOT NULL,
  fradrag_tillegg           VARCHAR2(7 CHAR) NOT NULL,
  type_sats                 VARCHAR2(10 CHAR) NOT NULL,
  bruk_kjoere_plan          VARCHAR2(5 CHAR) NOT NULL,
  saksbeh_id                VARCHAR2(10 CHAR) NOT NULL,
  utbetales_til_id          VARCHAR2(20 CHAR),
  henvisning                NUMBER(19) NOT NULL,
  ref_fagsystem_id          NUMBER(19),
  ref_delytelse_id          NUMBER(19),
  oppdrag110_id             NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_OPPDRAG_LINJE_150 PRIMARY KEY ( id )
);

ALTER TABLE OKO_OPPDRAG_LINJE_150 ADD CONSTRAINT FK_OKO_OPPDRAG_LINJE_150_1 FOREIGN KEY ( oppdrag110_id ) REFERENCES OKO_OPPDRAG_110 ( id );

ALTER TABLE OKO_OPPDRAG_LINJE_150 ADD CONSTRAINT CHK_OKO_OPPDRAG_LINJE_150_1 CHECK (KODE_ENDRING_LINJE IN ('NY', 'ENDR'));
ALTER TABLE OKO_OPPDRAG_LINJE_150 ADD CONSTRAINT CHK_OKO_OPPDRAG_LINJE_150_2 CHECK (KODE_STATUS_LINJE IN ('OPPH'));
ALTER TABLE OKO_OPPDRAG_LINJE_150 ADD CONSTRAINT CHK_OKO_OPPDRAG_LINJE_150_3 CHECK (TYPE_SATS IN ('DAG', 'UKE', 'MND', 'AAR', 'ENG', 'AKTO'));
ALTER TABLE OKO_OPPDRAG_LINJE_150 ADD CONSTRAINT CHK_OKO_OPPDRAG_LINJE_150_4
CHECK (KODE_KLASSIFIK IN ('FPATORD', 'FPATFRI', 'FPSND-OP', 'FPATAL', 'FPATSJO', 'FPSNDDM-OP', 'FPSNDJB-OP', 'FPSNDFI', 'FPATFER', 'FPREFAG-IOP', 'FPREFAGFER-IOP'));

CREATE INDEX IDX_OKO_OPPDRAG_LINJE_150_1 ON OKO_OPPDRAG_LINJE_150(vedtaksdato);
CREATE INDEX IDX_OKO_OPPDRAG_LINJE_150_2 ON OKO_OPPDRAG_LINJE_150(delytelse_id);
CREATE INDEX IDX_OKO_OPPDRAG_LINJE_150_3 ON OKO_OPPDRAG_LINJE_150(saksbeh_id);
CREATE INDEX IDX_OKO_OPPDRAG_LINJE_150_4 ON OKO_OPPDRAG_LINJE_150(henvisning);
CREATE INDEX IDX_OKO_OPPDRAG_LINJE_150_5 ON OKO_OPPDRAG_LINJE_150(oppdrag110_id);

CREATE SEQUENCE SEQ_OKO_OPPDRAG_LINJE_150 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_ATTESTANT_180
CREATE TABLE OKO_ATTESTANT_180 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  attestant_id              VARCHAR2(10 CHAR) NOT NULL,
  oppdraglinje150_id        NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_ATTESTANT_180 PRIMARY KEY ( id )
);

ALTER TABLE OKO_ATTESTANT_180 ADD CONSTRAINT FK_OKO_ATTESTANT_180_1 FOREIGN KEY ( oppdraglinje150_id ) REFERENCES OKO_OPPDRAG_LINJE_150 ( id );

CREATE INDEX IDX_OKO_ATTESTANT_180_1 ON OKO_ATTESTANT_180(attestant_id);
CREATE INDEX IDX_OKO_ATTESTANT_180_2 ON OKO_ATTESTANT_180(oppdraglinje150_id);

CREATE SEQUENCE SEQ_OKO_ATTESTANT_180 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_REFUSJONSINFO_156
CREATE TABLE OKO_REFUSJONSINFO_156 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  maks_dato                 DATE NOT NULL,
  refunderes_id             VARCHAR2(20 CHAR) NOT NULL,
  dato_fom                  DATE NOT NULL,
  oppdraglinje150_id        NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_REFUSJONSINFO_156 PRIMARY KEY ( id )
);

ALTER TABLE OKO_REFUSJONSINFO_156 ADD CONSTRAINT FK_OKO_REFUSJONSINFO_156_1 FOREIGN KEY ( oppdraglinje150_id ) REFERENCES OKO_OPPDRAG_LINJE_150 ( id );

CREATE INDEX IDX_OKO_REFUSJONSINFO_156_1 ON OKO_REFUSJONSINFO_156(oppdraglinje150_id);

CREATE SEQUENCE SEQ_OKO_REFUSJONSINFO_156 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OKO_GRAD_170
CREATE TABLE OKO_GRAD_170 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  type_grad                 VARCHAR2(10 CHAR) NOT NULL,
  grad                      NUMBER(5) NOT NULL,
  oppdraglinje150_id        NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_GRAD_170 PRIMARY KEY ( id )
);

ALTER TABLE OKO_GRAD_170 ADD CONSTRAINT FK_OKO_GRAD_170_1 FOREIGN KEY ( oppdraglinje150_id ) REFERENCES OKO_OPPDRAG_LINJE_150 ( id );

CREATE INDEX IDX_OKO_GRAD_170_1 ON OKO_GRAD_170(oppdraglinje150_id);

CREATE SEQUENCE SEQ_OKO_GRAD_170 MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell OPPDRAG_KVITTERING
CREATE TABLE OPPDRAG_KVITTERING (
  id                    NUMBER(19) NOT NULL,
  versjon               NUMBER(19) DEFAULT 0 NOT NULL,
  alvorlighetsgrad      VARCHAR2(2 CHAR),
  beskr_melding         VARCHAR2(300 CHAR),
  melding_kode          VARCHAR2(8 CHAR),
  oppdrag110_id         NUMBER(19) NOT NULL,
  opprettet_av          VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid         TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av             VARCHAR2(20 CHAR),
  endret_tid            TIMESTAMP(3),
  CONSTRAINT PK_OPPDRAG_KVITTERING PRIMARY KEY (id),
  CONSTRAINT FK_OPPDRAG_KVITTERING_1 FOREIGN KEY (oppdrag110_id) REFERENCES OKO_OPPDRAG_110(id)
);

CREATE SEQUENCE SEQ_OPPDRAG_KVITTERING MINVALUE 5000000 START WITH 5000000 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_OPPDRAG_KVITTERING_1 ON OPPDRAG_KVITTERING (OPPDRAG110_ID);


--- OPPDRAG_KONTROLL
COMMENT ON TABLE OPPDRAG_KONTROLL IS 'Inneholder referanse til behandling, saksnummer og prosess task';
COMMENT ON COLUMN OPPDRAG_KONTROLL.ID IS 'Primary key';
COMMENT ON COLUMN OPPDRAG_KONTROLL.BEHANDLING_ID IS 'FK: BEHANDLING';
COMMENT ON COLUMN OPPDRAG_KONTROLL.PROSESS_TASK_ID IS 'FK: PROSESS_TASK';
COMMENT ON COLUMN OPPDRAG_KONTROLL.SAKSNUMMER IS 'Saksnummer i GSAK';
COMMENT ON COLUMN OPPDRAG_KONTROLL.VENTER_KVITTERING IS 'Om oppdragskvittering er mottatt eller ikke';

-- OKO_OPPDRAG_110
COMMENT ON TABLE OKO_OPPDRAG_110 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_110.ID IS 'Primary key';
COMMENT ON COLUMN OKO_OPPDRAG_110.OPPDRAG_KONTROLL_ID IS 'FK: OPPDRAG_KONTROLL';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_AKSJON IS 'Aksjonskode 1 betyr at oppdragssystemet skal oppdateres. Aksjonskode 3 medfører en reell beregning, men det foretas ingen oppdatering av systemet.';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_ENDRING IS 'Benyttes for at systemet skal vite om det er et nytt oppdrag, endring i eksisterende oppdrag eller om det ikke er endring i informasjonen på oppdragsnivå (ny, endring, uendret)';
COMMENT ON COLUMN OKO_OPPDRAG_110.KODE_FAGOMRAADE IS 'Fagrutine';
COMMENT ON COLUMN OKO_OPPDRAG_110.FAGSYSTEM_ID IS 'Fagsystemets identifikasjon av stønaden/oppdraget';
COMMENT ON COLUMN OKO_OPPDRAG_110.UTBET_FREKVENS IS 'Angir med hvilken frekvens oppdraget skal beregnes/utbetales (DAG, UKE, MND, etc.)';
COMMENT ON COLUMN OKO_OPPDRAG_110.OPPDRAG_GJELDER_ID IS 'Angir hvem som saken/vedtaket er registrert på i fagrutinen, og må inneholde et gyldig fødselsnummer eller organisasjonsnummer';
COMMENT ON COLUMN OKO_OPPDRAG_110.DATO_OPPDRAG_GJELDER_FOM IS 'Dato oppdraget gjelder fra og med';
COMMENT ON COLUMN OKO_OPPDRAG_110.SAKSBEH_ID IS 'Må fylles ut for at Oppdragssystemet skal ha sporbarhet på hvem som har gjort endringer i data knyttet til det enkelte oppdrag';
COMMENT ON COLUMN OKO_OPPDRAG_110.AVSTEMMING115_ID IS 'FK: Midlertidig, skal fjernes etter fk-forholdet blir fikset';

-- OKO_AVSTEMMING_115
COMMENT ON TABLE OKO_AVSTEMMING_115 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_AVSTEMMING_115.ID IS 'Primary key';
COMMENT ON COLUMN OKO_AVSTEMMING_115.OPPDRAG110_ID IS 'FK: OKO_OPPDRAG_110';
COMMENT ON COLUMN OKO_AVSTEMMING_115.KODE_KOMPONENT IS 'Identifiserer avleverende komponent av dataene (brukes ved avstemming)';
COMMENT ON COLUMN OKO_AVSTEMMING_115.NOEKKEL_AVSTEMMING IS 'Brukes til å identifisere data som skal avstemmes';
COMMENT ON COLUMN OKO_AVSTEMMING_115.TIDSPUNKT_MELDING IS 'Når meldingen ble sendt';

-- OKO_OPPDRAG_ENHET_120
COMMENT ON TABLE OKO_OPPDRAG_ENHET_120 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.ID IS 'Primary key';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.OPPDRAG110_ID IS 'FK: OKO_OPPDRAG_110';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.TYPE_ENHET IS 'Angir hva slags type enhet som mottas (f.eks bosted, behandlende)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.ENHET IS 'Identifiserer den aktuelle enheten (tknr evt. orgnr + avd)';
COMMENT ON COLUMN OKO_OPPDRAG_ENHET_120.DATO_ENHET_FOM IS 'Angir når ny oppdragsenhet gjelder fra';

-- OKO_OPPDRAG_LINJE_150
COMMENT ON TABLE OKO_OPPDRAG_LINJE_150 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.ID IS 'Primary key';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.OPPDRAG110_ID IS 'FK: OKO_OPPDRAG_110';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_ENDRING_LINJE IS 'Benyttes for at systemet skal vite om det er en ny oppdragslinje eller endring i eksisterende linje';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_STATUS_LINJE IS 'Benyttes for å påvirke behandlingen av den enkelte delytelsen. (opphør, hvilende, sperret, reaktiver)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_STATUS_FOM IS 'Er hvilken beregningsperiode for delytelsen som statusen skal gjelde fra';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.VEDTAKSDATO IS 'Vedtaksdato';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DELYTELSE_ID IS 'Basert på fagsystemets id med start fra 100 for tre siste sifre i id';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_VEDTAK_FOM IS 'Utbetaling/vedtak fom';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.DATO_VEDTAK_TOM IS 'Utbetaling/vedtak tom';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.KODE_KLASSIFIK IS 'Skal entydig bestemme hvilket kontonummer delytelsen regnskapsføres på, og må defineres i nært samarbeid med Oppdragssystemet etterhvert som nye fagområder tilknyttes systemet.';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.SATS IS 'Engangsstønad sats som ble brukt til beregning';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.FRADRAG_TILLEGG IS 'Angir om satsen går til fradag eller utbetaling (T = tillegg, F = fradrag))';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.TYPE_SATS IS 'Satstype (engangs, dag, 14-dag, uke, mnd, år)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.BRUK_KJOERE_PLAN IS 'Angir om utbetaling skal skje i dag eller i henhold til kjøreplan';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.SAKSBEH_ID IS 'Ansvarlig saksbehandler';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.UTBETALES_TIL_ID IS 'Utbetalingsmottaker (fnr/orgnr)';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.HENVISNING is 'Behandlingsid, brukes ved avstemming';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.REF_FAGSYSTEM_ID IS 'Oppdragsbasens identifikasjon av vedtaket som endres';
COMMENT ON COLUMN OKO_OPPDRAG_LINJE_150.REF_DELYTELSE_ID IS 'Identifikasjon av delytelsen som endres (fagsystemets id)';

-- OKO_ATTESTANT_180
COMMENT ON TABLE OKO_ATTESTANT_180 IS 'Inneholder de relevante verdier som mappes og sender med i request melding. (Tabell fra Økonomi)';
COMMENT ON COLUMN OKO_ATTESTANT_180.ID IS 'Primary key';
COMMENT ON COLUMN OKO_ATTESTANT_180.OPPDRAGLINJE150_ID IS 'FK: OKO_OPPDRAG_LINJE_150';
COMMENT ON COLUMN OKO_ATTESTANT_180.ATTESTANT_ID IS 'Saksbehandlers ID fra vedtaket';

-- OKO_GRAD_170
COMMENT ON TABLE OKO_GRAD_170 IS 'Graderingsinformasjon for økonomioppdrag';
COMMENT ON COLUMN OKO_GRAD_170.ID IS 'Primary key';
COMMENT ON COLUMN OKO_GRAD_170.OPPDRAGLINJE150_ID IS 'FK: OKO_OPPDRAG_LINJE_150';
COMMENT ON COLUMN OKO_GRAD_170.TYPE_GRAD IS 'Hva slags grad som mottas';
COMMENT ON COLUMN OKO_GRAD_170.GRAD IS 'Grad, prosent';

-- OKO_REFUSJONSINFO_156
COMMENT ON TABLE OKO_REFUSJONSINFO_156 IS 'Refusjonsinformasjon for økonomioppdrag';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.ID IS 'Primary key';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.OPPDRAGLINJE150_ID IS 'FK: OKO_OPPDRAG_LINJE_150';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.DATO_FOM IS 'Dato refusjonsinformasjon gjelder fra';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.MAKS_DATO IS 'Maks dato for ytelsen';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.REFUNDERES_ID IS 'Arbeidsgivers organisasjonsnummer';

-- OPPDRAG_KVITTERING
COMMENT ON TABLE OPPDRAG_KVITTERING IS 'Kvittering fra oppdragssystemet';
COMMENT ON COLUMN OPPDRAG_KVITTERING.ID IS 'Primary key';
COMMENT ON COLUMN OPPDRAG_KVITTERING.OPPDRAG110_ID IS 'FK: OKO_OPPDRAG_110';
COMMENT ON COLUMN OPPDRAG_KVITTERING.ALVORLIGHETSGRAD IS '00-Ok, 04-Ok med varsel, 08-Avvist av oppdrag, 12-Intern feil i oppdrag';
COMMENT ON COLUMN OPPDRAG_KVITTERING.BESKR_MELDING IS 'Feiltekst / meldingstekst fra økonomioppdrag';
COMMENT ON COLUMN OPPDRAG_KVITTERING.MELDING_KODE IS 'Feilmeldingskode fra økonomioppdrag';


