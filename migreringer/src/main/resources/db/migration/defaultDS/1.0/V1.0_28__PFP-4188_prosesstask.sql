--------------------------------------------------------
--  DDL for PROSESS_TASK_FEILHAND
--------------------------------------------------------

  CREATE TABLE PROSESS_TASK_FEILHAND (
    KODE            VARCHAR2(20 CHAR)                     NOT NULL,
	  NAVN            VARCHAR2(50 CHAR)                     NOT NULL,
	  BESKRIVELSE     VARCHAR2(2000 CHAR),
	  INPUT_VARIABEL1 NUMBER,
	  INPUT_VARIABEL2 NUMBER,
	  OPPRETTET_AV    VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
	  OPPRETTET_TID   TIMESTAMP (3)     DEFAULT systimestamp NOT NULL,
	  ENDRET_AV       VARCHAR2(20 CHAR),
	  ENDRET_TID      TIMESTAMP (3),
	  CONSTRAINT PK_PROSESS_TASK_FEILHAND PRIMARY KEY (KODE)
   );

   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.KODE IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.NAVN IS 'Lesbart navn på type feilhåndtering brukt i prosesstask';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.BESKRIVELSE IS 'Utdypende beskrivelse av koden';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.INPUT_VARIABEL1 IS 'Variabel 1: Dynamisk konfigurasjon for en feilhåndteringsstrategi.  Verdi og betydning er bestemt av feilhåndteringsalgoritmen';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.INPUT_VARIABEL2 IS 'Variabel 2: Dynamisk konfigurasjon for en feilhåndteringsstrategi.  Verdi og betydning er bestemt av feilhåndteringsalgoritmen';
   COMMENT ON TABLE PROSESS_TASK_FEILHAND  IS 'Kodetabell for feilhåndterings-metoder. For eksempel antall ganger å prøve på nytt og til hvilke tidspunkt';

--------------------------------------------------------
--  DDL for PROSESS_TASK_TYPE
--------------------------------------------------------

  CREATE TABLE PROSESS_TASK_TYPE (
    KODE                     VARCHAR2(50 CHAR)                        NOT NULL,
	  NAVN                     VARCHAR2(50 CHAR),
	  FEIL_MAKS_FORSOEK        NUMBER(10,0)        DEFAULT 1            NOT NULL,
	  FEIL_SEK_MELLOM_FORSOEK  NUMBER(10,0)        DEFAULT 30           NOT NULL,
	  FEILHANDTERING_ALGORITME VARCHAR2(20 CHAR)   DEFAULT 'DEFAULT',
	  BESKRIVELSE              VARCHAR2(2000 CHAR),
	  OPPRETTET_AV             VARCHAR2(20 CHAR)   DEFAULT 'VL'         NOT NULL,
	  OPPRETTET_TID            TIMESTAMP (3)       DEFAULT systimestamp NOT NULL,
	  ENDRET_AV                VARCHAR2(20 CHAR),
	  ENDRET_TID               TIMESTAMP (3),
	  CONSTRAINT PK_PROSESS_TASK_TYPE   PRIMARY KEY (KODE),
	  CONSTRAINT FK_PROSESS_TASK_TYPE_1 FOREIGN KEY (FEILHANDTERING_ALGORITME) REFERENCES PROSESS_TASK_FEILHAND (KODE)
   );

   COMMENT ON COLUMN PROSESS_TASK_TYPE.KODE IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.NAVN IS 'Lesbart navn på prosesstasktype';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEIL_MAKS_FORSOEK IS 'Maksimalt anntall forsøk på rekjøring om noe går galt';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEIL_SEK_MELLOM_FORSOEK IS 'Ventetid i sekunder mellom hvert forsøk på rekjøring om noe har gått galt';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEILHANDTERING_ALGORITME IS 'FK:PROSESS_TASK_FEILHAND Fremmednøkkel til tabell som viser detaljer om hvordan en feilsituasjon skal håndteres';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';
   COMMENT ON TABLE PROSESS_TASK_TYPE  IS 'Kodetabell for typer prosesser med beskrivelse og informasjon om hvilken feilhåndteringen som skal benyttes';

  CREATE INDEX IDX_PROSESS_TASK_TYPE_1 ON PROSESS_TASK_TYPE (FEILHANDTERING_ALGORITME);



Declare

  opprett_process_tabell varchar2(2000) :=
                                       ' CREATE TABLE PROSESS_TASK ' ||
                                       ' ( ID NUMBER(19,0) NOT NULL ENABLE, ' ||
                                       ' TASK_TYPE VARCHAR2(50 CHAR) NOT NULL ENABLE, ' ||
                                       ' PRIORITET NUMBER(3,0) DEFAULT 0 NOT NULL ENABLE, ' ||
                                       ' STATUS VARCHAR2(20 CHAR) DEFAULT ''KLAR'' NOT NULL ENABLE, ' ||
                                       ' TASK_PARAMETERE VARCHAR2(4000 CHAR), ' ||
                                       ' TASK_PAYLOAD CLOB, ' ||
                                       ' TASK_GRUPPE VARCHAR2(250 CHAR), ' ||
                                       ' TASK_SEKVENS VARCHAR2(100 CHAR) DEFAULT ''1'' NOT NULL ENABLE, ' ||
                                       ' NESTE_KJOERING_ETTER TIMESTAMP (0) DEFAULT current_timestamp, ' ||
                                       ' FEILEDE_FORSOEK NUMBER(5,0) DEFAULT 0, ' ||
                                       ' SISTE_KJOERING_TS TIMESTAMP (6), ' ||
                                       ' SISTE_KJOERING_SLUTT_TS timestamp(6), ' ||
                                       ' SISTE_KJOERING_FEIL_KODE VARCHAR2(50 CHAR), ' ||
                                       ' SISTE_KJOERING_FEIL_TEKST CLOB, ' ||
                                       ' SISTE_KJOERING_SERVER VARCHAR2(50 CHAR), ' ||
                                       ' VERSJON NUMBER(19,0) DEFAULT 0 NOT NULL ENABLE, ' ||
                                       ' BLOKKERT_AV NUMBER(19, 0), ' ||
	                                     ' OPPRETTET_AV varchar2(20 char)  default ''VL''  not null, ' ||
	                                     ' OPPRETTET_TID TIMESTAMP(6) DEFAULT systimestamp  NOT NULL, ' ||
                                       ' CONSTRAINT CHK_PROSESS_TASK_STATUS CHECK (status IN (''KLAR'', ''FEILET'', ''VENTER_SVAR'', ''SUSPENDERT'', ''FERDIG'', ''VETO'')) ENABLE, ' ||
                                       ' CONSTRAINT PK_PROSESS_TASK PRIMARY KEY (ID), ' ||
                                       ' CONSTRAINT FK_PROSESS_TASK_1 FOREIGN KEY (TASK_TYPE)REFERENCES PROSESS_TASK_TYPE (KODE) ENABLE) enable row movement';

  legg_partisjon varchar2(255) := ' PARTITION by list (status)(' ||
                                      ' PARTITION status_ferdig values (''FERDIG''),' ||
                                      ' PARTITION status_feilet values (''FEILET''),' ||
                                      ' PARTITION status_klar values(''KLAR'', ''VENTER_SVAR'', ''SUSPENDERT'', ''VETO''))';

BEGIN
  IF (DBMS_DB_VERSION.VERSION < 12) THEN
    execute immediate opprett_process_tabell;
  ELSE
    execute immediate opprett_process_tabell || legg_partisjon;
  END IF;
END;

