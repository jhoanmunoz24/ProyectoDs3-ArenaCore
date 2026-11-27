CREATE TABLE reservas (
    id UUID PRIMARY KEY,
    codigo_reserva VARCHAR(40) NOT NULL UNIQUE,
    cancha_id UUID NOT NULL REFERENCES canchas(id),
    cliente_id UUID,
    nombre_capitan VARCHAR(120) NOT NULL,
    telefono_capitan VARCHAR(30) NOT NULL,
    nombre_equipo VARCHAR(120),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    duracion_horas INT NOT NULL CHECK (duracion_horas >= 1),
    estado VARCHAR(30) NOT NULL,
    abono_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    motivo_cancelacion TEXT,
    autorizacion_excepcional_id UUID,
    creado_por_id UUID NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sesiones_juego (
    id UUID PRIMARY KEY,
    reserva_id UUID REFERENCES reservas(id),
    cancha_id UUID NOT NULL REFERENCES canchas(id),
    nombre_capitan VARCHAR(120) NOT NULL,
    telefono_capitan VARCHAR(30) NOT NULL,
    nombre_equipo VARCHAR(120),
    fecha DATE NOT NULL,
    hora_entrada TIMESTAMPTZ NOT NULL,
    hora_salida_programada TIMESTAMPTZ NOT NULL,
    hora_salida_real TIMESTAMPTZ,
    duracion_horas_reservadas INT NOT NULL CHECK (duracion_horas_reservadas >= 1),
    liquidacion_id UUID,
    estado VARCHAR(30) NOT NULL,
    operador_id UUID NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE eventos_sesion_juego (
    id UUID PRIMARY KEY,
    sesion_juego_id UUID NOT NULL REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(40) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cancha_anterior_id UUID,
    cancha_nueva_id UUID,
    usuario_id UUID NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
