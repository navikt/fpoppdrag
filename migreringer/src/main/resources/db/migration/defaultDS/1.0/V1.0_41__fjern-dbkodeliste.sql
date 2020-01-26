
alter table SIMULERT_POSTERING set unused column KL_FAG_OMRAADE_KODE;
alter table SIMULERT_POSTERING set unused column KL_BETALING_TYPE;
alter table SIMULERT_POSTERING set unused column KL_POSTERING_TYPE;
alter table SIMULERT_POSTERING set unused column KL_KLASSE_KODE;
alter table SIMULERT_POSTERING set unused column KL_SATS_TYPE;

alter table GR_SIMULERING set unused column KL_YTELSE_TYPE;
alter table SIMULERING_MOTTAKER set unused column KL_MOTTAKER_TYPE;
alter table TILKJENT_YTELSE_ANDEL set unused column KL_INNTEKTSKATEGORI;

drop table kodeliste cascade constraints purge ;
drop table kodeverk cascade constraints purge ;

drop sequence seq_kodeliste;