BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE flyway_schema_history AS (SELECT * FROM schema_version)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;

BEGIN
    EXECUTE IMMEDIATE 'INSERT INTO flyway_schema_history (SELECT * FROM schema_version);';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;

