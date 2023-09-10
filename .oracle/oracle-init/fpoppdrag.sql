DECLARE userexists INTEGER;
BEGIN
    SELECT count(*)
    INTO userexists
    FROM SYS.ALL_USERS
    WHERE USERNAME = 'FPOPPDRAG';
    IF (userexists = 0)
    THEN
        EXECUTE IMMEDIATE ('CREATE USER FPOPPDRAG IDENTIFIED BY fpoppdrag PROFILE DEFAULT ACCOUNT UNLOCK');
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

ALTER USER FPOPPDRAG QUOTA UNLIMITED ON SYSTEM;

DECLARE userexists INTEGER;
BEGIN
    SELECT count(*)
    INTO userexists
    FROM SYS.ALL_USERS
    WHERE USERNAME = 'FPOPPDRAG_UNIT';
    IF (userexists = 0)
    THEN
        EXECUTE IMMEDIATE ('CREATE USER FPOPPDRAG_UNIT IDENTIFIED BY fpoppdrag_unit PROFILE DEFAULT ACCOUNT UNLOCK');
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
    TO FPOPPDRAG_UNIT;
