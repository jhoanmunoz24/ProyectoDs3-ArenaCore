-- RNF-02: Separación de credenciales de migración (Flyway) y aplicación (JPA) en db_billing
CREATE ROLE billing_migrator WITH LOGIN PASSWORD 'billing_mig_pass';
CREATE ROLE billing_app WITH LOGIN PASSWORD 'billing_app_pass';

GRANT ALL PRIVILEGES ON DATABASE db_billing TO billing_migrator;
GRANT USAGE, CREATE ON SCHEMA public TO billing_migrator;
GRANT USAGE ON SCHEMA public TO billing_app;

ALTER DEFAULT PRIVILEGES FOR ROLE billing_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO billing_app;
ALTER DEFAULT PRIVILEGES FOR ROLE billing_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO billing_app;
