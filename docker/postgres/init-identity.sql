-- RNF-02: Separación de credenciales de migración (Flyway) y aplicación (JPA) en db_identity
CREATE ROLE identity_migrator WITH LOGIN PASSWORD 'identity_mig_pass';
CREATE ROLE identity_app WITH LOGIN PASSWORD 'identity_app_pass';

GRANT ALL PRIVILEGES ON DATABASE db_identity TO identity_migrator;
GRANT USAGE, CREATE ON SCHEMA public TO identity_migrator;
GRANT USAGE ON SCHEMA public TO identity_app;

ALTER DEFAULT PRIVILEGES FOR ROLE identity_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO identity_app;
ALTER DEFAULT PRIVILEGES FOR ROLE identity_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO identity_app;
