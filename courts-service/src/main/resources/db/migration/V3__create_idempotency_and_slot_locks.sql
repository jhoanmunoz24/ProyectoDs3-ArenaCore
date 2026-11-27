CREATE TABLE bloques_ocupados (
    id UUID PRIMARY KEY,
    cancha_id UUID NOT NULL REFERENCES canchas(id),
    fecha DATE NOT NULL,
    bloque_hora INT NOT NULL CHECK (bloque_hora BETWEEN 7 AND 22),
    reserva_id UUID REFERENCES reservas(id),
    sesion_juego_id UUID REFERENCES sesiones_juego(id),
    estado VARCHAR(20) NOT NULL
);

-- Prevención de Overbooking a nivel de motor PostgreSQL (Regla de Negocio + RNF)
CREATE UNIQUE INDEX uk_bloques_ocupados_activo
    ON bloques_ocupados (cancha_id, fecha, bloque_hora)
    WHERE estado IN ('CONFIRMADA', 'ACTIVA');

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    endpoint VARCHAR(120) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_status INT NOT NULL,
    response_body TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (idempotency_key, endpoint)
);

CREATE INDEX idx_reservas_fecha_cancha ON reservas(fecha, cancha_id);
CREATE INDEX idx_sesiones_estado ON sesiones_juego(estado);
