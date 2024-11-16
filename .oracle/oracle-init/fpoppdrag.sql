ALTER SESSION SET CONTAINER=FREEPDB1;

DECLARE
    userexists INTEGER;
BEGIN
    SELECT count(*)
    INTO userexists
    FROM SYS.ALL_USERS
    WHERE USERNAME = 'FPOPPDRAG';
    IF (userexists = 0)
    THEN
        EXECUTE IMMEDIATE ('CREATE USER FPOPPDRAG IDENTIFIED BY fpoppdrag PROFILE DEFAULT ACCOUNT UNLOCK QUOTA UNLIMITED ON SYSTEM');
    END IF;
END;
/

GRANT
    CREATE SESSION,
    ALTER SESSION,
    CONNECT,
    RESOURCE,
    CREATE MATERIALIZED VIEW,
    CREATE JOB,
    CREATE TABLE,
    CREATE SYNONYM,
    CREATE VIEW,
    CREATE SEQUENCE,
    UNLIMITED TABLESPACE,
    SELECT ANY TABLE
    TO FPOPPDRAG;
