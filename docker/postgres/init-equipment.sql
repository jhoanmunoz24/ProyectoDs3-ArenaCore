-- RNF-02: Separación de credenciales de migración (Flyway) y aplicación (JPA) en db_equipment
CREATE ROLE equipment_migrator WITH LOGIN PASSWORD 'equipment_mig_pass';
CREATE ROLE equipment_app WITH LOGIN PASSWORD 'equipment_app_pass';

GRANT ALL PRIVILEGES ON DATABASE db_equipment TO equipment_migrator;
GRANT USAGE, CREATE ON SCHEMA public TO equipment_migrator;
GRANT USAGE ON SCHEMA public TO equipment_app;

ALTER DEFAULT PRIVILEGES FOR ROLE equipment_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO equipment_app;
ALTER DEFAULT PRIVILEGES FOR ROLE equipment_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO equipment_app;
