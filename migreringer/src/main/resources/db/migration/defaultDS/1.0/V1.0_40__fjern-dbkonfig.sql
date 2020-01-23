drop table konfig_verdi cascade constraints purge ;
drop table konfig_verdi_kode cascade constraints purge ;

drop sequence seq_konfig_verdi;

alter table SIMULERT_POSTERING drop constraint FK_SIMULERT_POSTERING_02;
alter table SIMULERT_POSTERING drop constraint FK_SIMULERT_POSTERING_03;
alter table SIMULERT_POSTERING drop constraint FK_SIMULERT_POSTERING_04;
alter table SIMULERT_POSTERING drop constraint FK_SIMULERT_POSTERING_05;
alter table SIMULERT_POSTERING drop constraint FK_SIMULERT_POSTERING_06;

alter table GR_SIMULERING drop constraint FK_GR_SIMULERING_02;
alter table SIMULERING_MOTTAKER drop constraint FK_SIMULERING_MOTTAKER_02;
alter table TILKJENT_YTELSE_ANDEL drop constraint FK_TILKJENT_YTELSE_ANDEL_2;
