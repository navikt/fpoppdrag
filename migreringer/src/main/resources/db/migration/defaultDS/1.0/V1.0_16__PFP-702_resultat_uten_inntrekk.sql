ALTER TABLE SIMULERT_POSTERING ADD uten_inntrekk VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL;

COMMENT ON COLUMN SIMULERT_POSTERING.uten_inntrekk IS 'Angir om inntrekk var slått av ved kall til simuleringstjenesten.';