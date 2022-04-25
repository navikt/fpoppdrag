ALTER TABLE GR_SIMULERING MODIFY AKTOER_ID NOT NULL;

ALTER TABLE SIMULERING_MOTTAKER MODIFY MOTTAKER_NUMMER NOT NULL;
ALTER TABLE SIMULERING_MOTTAKER SET UNUSED COLUMN ENDRET_AV;
ALTER TABLE SIMULERING_MOTTAKER SET UNUSED COLUMN ENDRET_TID;

ALTER TABLE SIMULERT_POSTERING MODIFY POSTERING_TYPE NULL;
ALTER TABLE SIMULERT_POSTERING SET UNUSED COLUMN ENDRET_AV;
ALTER TABLE SIMULERT_POSTERING SET UNUSED COLUMN ENDRET_TID;

ALTER TABLE SIMULERING_XML SET UNUSED COLUMN ENDRET_AV;
ALTER TABLE SIMULERING_XML SET UNUSED COLUMN ENDRET_TID;

ALTER TABLE SIMULERING_RESULTAT SET UNUSED COLUMN ENDRET_AV;
ALTER TABLE SIMULERING_RESULTAT SET UNUSED COLUMN ENDRET_TID;
