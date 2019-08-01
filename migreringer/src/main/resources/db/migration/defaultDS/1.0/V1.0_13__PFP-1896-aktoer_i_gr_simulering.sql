alter table GR_SIMULERING add AKTOER_ID VARCHAR2(50 CHAR);

comment on column GR_SIMULERING.AKTOER_ID is 'AktørId for bruker';