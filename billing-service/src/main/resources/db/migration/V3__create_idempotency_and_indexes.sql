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

CREATE UNIQUE INDEX uk_sesion_caja_abierta_por_caja ON sesiones_caja (caja_id) WHERE estado = 'ABIERTA';
CREATE INDEX idx_liquidaciones_estado ON liquidaciones(estado);
CREATE INDEX idx_pagos_liquidacion ON pagos(liquidacion_id);
CREATE INDEX idx_movimientos_sesion_caja ON movimientos_caja(sesion_caja_id);
