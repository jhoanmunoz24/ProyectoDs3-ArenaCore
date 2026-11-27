CREATE TABLE sedes (
    id UUID PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    hora_apertura TIME NOT NULL DEFAULT '07:00:00',
    hora_cierre TIME NOT NULL DEFAULT '23:00:00',
    zona_horaria VARCHAR(60) NOT NULL DEFAULT 'America/Bogota',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE canchas (
    id UUID PRIMARY KEY,
    sede_id UUID NOT NULL REFERENCES sedes(id),
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    capacidad_jugadores INT NOT NULL,
    superficie VARCHAR(80) NOT NULL DEFAULT 'GRAMA_SINTETICA',
    tiene_iluminacion BOOLEAN NOT NULL DEFAULT TRUE,
    estado_operativo VARCHAR(30) NOT NULL DEFAULT 'HABILITADA',
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE mantenimientos_cancha (
    id UUID PRIMARY KEY,
    cancha_id UUID NOT NULL REFERENCES canchas(id),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
