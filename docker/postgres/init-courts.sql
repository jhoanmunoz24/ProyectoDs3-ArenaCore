-- RNF-02: Separación de credenciales de migración (Flyway) y aplicación (JPA) en db_courts
CREATE ROLE courts_migrator WITH LOGIN PASSWORD 'courts_mig_pass';
CREATE ROLE courts_app WITH LOGIN PASSWORD 'courts_app_pass';

GRANT ALL PRIVILEGES ON DATABASE db_courts TO courts_migrator;
GRANT USAGE, CREATE ON SCHEMA public TO courts_migrator;
GRANT USAGE ON SCHEMA public TO courts_app;

ALTER DEFAULT PRIVILEGES FOR ROLE courts_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO courts_app;
ALTER DEFAULT PRIVILEGES FOR ROLE courts_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO courts_app;
