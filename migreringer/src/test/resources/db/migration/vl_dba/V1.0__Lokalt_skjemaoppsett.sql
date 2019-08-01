-- ##################################################
-- ### Opplegg for enhetstester (lokal + jenkins) ###
-- ##################################################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = upper('${fpoppdrag_schema_unit}');
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER ${fpoppdrag_schema_unit} IDENTIFIED BY ${fpoppdrag_schema_unit}');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO ${fpoppdrag_schema_unit};

-- ###############################
-- ### Opplegg for lokal jetty ###
-- ###############################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPOPPDRAG';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPOPPDRAG IDENTIFIED BY fpoppdrag');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO FPOPPDRAG;