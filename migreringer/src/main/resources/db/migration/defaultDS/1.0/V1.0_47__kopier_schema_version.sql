CREATE TABLE flyway_schema_history AS (SELECT * FROM schema_version);
INSERT INTO flyway_schema_history (SELECT * FROM schema_version);