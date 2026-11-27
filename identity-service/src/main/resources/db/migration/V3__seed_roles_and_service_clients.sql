INSERT INTO roles (id, nombre, descripcion) VALUES
    ('11111111-1111-1111-1111-111111111101', 'ROLE_ADMINISTRADOR', 'Administrador total del complejo deportivo, usuarios, tarifas y arqueos'),
    ('11111111-1111-1111-1111-111111111102', 'ROLE_OPERADOR', 'Recepcionista / operador de agenda, check-in, préstamos y caja'),
    ('11111111-1111-1111-1111-111111111103', 'ROLE_CLIENTE', 'Capitán de equipo o cliente para consulta y reservas');

CREATE TABLE service_clients (
    client_id VARCHAR(80) PRIMARY KEY,
    client_secret_hash VARCHAR(255) NOT NULL,
    service_name VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
