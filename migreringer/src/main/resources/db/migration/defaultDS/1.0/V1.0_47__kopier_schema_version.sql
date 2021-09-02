declare
    c int;
begin
    select count(*) into c from user_tables where table_name = 'schema_version';
    if c = 1 then
        EXECUTE IMMEDIATE 'CREATE TABLE flyway_schema_history AS (SELECT * FROM schema_version)';
        EXECUTE IMMEDIATE 'INSERT INTO flyway_schema_history (SELECT * FROM schema_version)';
    end if;
end;
