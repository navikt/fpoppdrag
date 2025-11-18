create sequence SEQ_SIMULERING_RESULTAT
    increment by 50
    nocache;

create sequence SEQ_GR_SIMULERING
    increment by 50
    nocache;

create sequence SEQ_SIMULERING_MOTTAKER
    increment by 50
    nocache;

create sequence SEQ_SIMULERT_POSTERING
    increment by 50
    nocache;

create table SIMULERING_RESULTAT
(
    ID            NUMBER(19) not null
        constraint PK_SIMULERING_RESULTAT
        primary key,
    OPPRETTET_AV  VARCHAR2(200 char) default 'VL',
    OPPRETTET_TID TIMESTAMP(3)       default systimestamp
);

comment on table SIMULERING_RESULTAT is 'Inneholder lister av simuleringResultat resultat';

comment on column SIMULERING_RESULTAT.ID is 'Primary Key';

create table GR_SIMULERING
(
    ID                     NUMBER(19)                     not null
        constraint PK_GR_SIMULERING
        primary key,
    BEHANDLING_ID          NUMBER(19)                     not null,
    SIMULERING_ID          NUMBER(19)                     not null
        constraint FK_GR_SIMULERING_01
        references SIMULERING_RESULTAT,
    AKTIV                  VARCHAR2(1 char)   default 'N' not null,
    VERSJON                NUMBER(19)         default 0   not null,
    OPPRETTET_AV           VARCHAR2(200 char) default 'VL',
    OPPRETTET_TID          TIMESTAMP(3)       default systimestamp,
    ENDRET_AV              VARCHAR2(200 char),
    ENDRET_TID             TIMESTAMP(3),
    AKTOER_ID              VARCHAR2(50 char)              not null,
    SIMULERING_KJOERT_DATO TIMESTAMP(3)                   not null,
    YTELSE_TYPE            VARCHAR2(100 char) default '-' not null
);

comment on table GR_SIMULERING is 'Inneholder version liste av simuleringResultat';

comment on column GR_SIMULERING.ID is 'Primary Key';

comment on column GR_SIMULERING.BEHANDLING_ID is 'FK:BEHANDLING Foreign key';

comment on column GR_SIMULERING.SIMULERING_ID is 'FK:Simulering';

comment on column GR_SIMULERING.AKTIV is 'Angir status av Simulering';

comment on column GR_SIMULERING.VERSJON is 'Version av simuleringResultat';

comment on column GR_SIMULERING.AKTOER_ID is 'AktørId for bruker';

comment on column GR_SIMULERING.SIMULERING_KJOERT_DATO is 'Tidspunkt for når simulering ble utført.';

comment on column GR_SIMULERING.YTELSE_TYPE is 'Type ytelse til ekstern behandling';

create index IDX_GR_SIMULERING_1
    on GR_SIMULERING (SIMULERING_ID);

create index IDX_GR_SIMULERING_2
    on GR_SIMULERING (YTELSE_TYPE);

create index IDX_GR_SIMULERING_3
    on GR_SIMULERING (BEHANDLING_ID);

create table SIMULERING_MOTTAKER
(
    ID              NUMBER(19)         not null
        constraint PK_SIMULERING_MOTTAKER
        primary key,
    SIMULERING_ID   NUMBER(19)         not null
        constraint FK_SIMULERING_MOTTAKER_01
        references SIMULERING_RESULTAT,
    MOTTAKER_NUMMER VARCHAR2(50 char)  not null,
    MOTTAKER_TYPE   VARCHAR2(100 char) not null,
    OPPRETTET_AV    VARCHAR2(200 char) default 'VL',
    OPPRETTET_TID   TIMESTAMP(3)       default systimestamp
);

comment on table SIMULERING_MOTTAKER is 'Inneholder mottaker informasjon';

comment on column SIMULERING_MOTTAKER.MOTTAKER_NUMMER is 'Utbetalingsmottaker (fnr/orgnr)';

comment on column SIMULERING_MOTTAKER.MOTTAKER_TYPE is 'Type av mottaker';

create index IDX_SIMULERING_MOTTAKER_1
    on SIMULERING_MOTTAKER (SIMULERING_ID);

create index IDX_SIMULERING_MOTTAKER_2
    on SIMULERING_MOTTAKER (MOTTAKER_TYPE);

create table SIMULERT_POSTERING
(
    ID                     NUMBER(19)                     not null
        constraint PK_SIMULERT_POSTERING
        primary key,
    SIMULERING_MOTTAKER_ID NUMBER(19)                     not null
        constraint FK_SIMULERT_POSTERING_01
        references SIMULERING_MOTTAKER,
    FAG_OMRAADE_KODE       VARCHAR2(100 char)             not null,
    FOM                    DATE                           not null,
    TOM                    DATE                           not null,
    BETALING_TYPE          VARCHAR2(100 char)             not null,
    BELOEP                 NUMBER(12, 2)                  not null
        check (BELOEP >= 0),
    POSTERING_TYPE         VARCHAR2(100 char),
    OPPRETTET_AV           VARCHAR2(200 char) default 'VL',
    OPPRETTET_TID          TIMESTAMP(3)       default systimestamp,
    FORFALL                DATE                           not null,
    UTEN_INNTREKK          VARCHAR2(1 char)   default 'N' not null
);

comment on table SIMULERT_POSTERING is 'Inneholder detaljer av simuleringResultat';

comment on column SIMULERT_POSTERING.ID is 'Primary Key';

comment on column SIMULERT_POSTERING.SIMULERING_MOTTAKER_ID is 'FK:SIMULERING_MOTTAKER';

comment on column SIMULERT_POSTERING.FAG_OMRAADE_KODE is 'Fagområde.Mulige verdier: FP, SP, PP';

comment on column SIMULERT_POSTERING.FOM is 'Første dag av Simulering Postering';

comment on column SIMULERT_POSTERING.TOM is 'Siste dag av Simulering Postering';

comment on column SIMULERT_POSTERING.BETALING_TYPE is 'Angir om det er kredit-post eller debet-post';

comment on column SIMULERT_POSTERING.BELOEP is 'Beloep av Simulering Postering';

comment on column SIMULERT_POSTERING.POSTERING_TYPE is 'Type av postering';

comment on column SIMULERT_POSTERING.FORFALL is 'Forfalls dato fra simulering resultat';

comment on column SIMULERT_POSTERING.UTEN_INNTREKK is 'Angir om inntrekk var slått av ved kall til simuleringstjenesten.';

create index IDX_SIMULERT_POSTERING_1
    on SIMULERT_POSTERING (SIMULERING_MOTTAKER_ID);

create index IDX_SIMULERT_POSTERING_2
    on SIMULERT_POSTERING (FAG_OMRAADE_KODE);

create index IDX_SIMULERT_POSTERING_3
    on SIMULERT_POSTERING (BETALING_TYPE);

create index IDX_SIMULERT_POSTERING_4
    on SIMULERT_POSTERING (POSTERING_TYPE);
