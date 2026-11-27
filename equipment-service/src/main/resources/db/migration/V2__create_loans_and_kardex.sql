CREATE TABLE prestamos (
    id UUID PRIMARY KEY,
    sesion_juego_id UUID NOT NULL,
    operador_id UUID NOT NULL,
    estado VARCHAR(30) NOT NULL,
    observaciones TEXT,
    fecha_prestamo TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_devolucion TIMESTAMPTZ
);

CREATE TABLE detalles_prestamo (
    id UUID PRIMARY KEY,
    prestamo_id UUID NOT NULL REFERENCES prestamos(id) ON DELETE CASCADE,
    implemento_id UUID NOT NULL REFERENCES implementos(id),
    cantidad_entregada INT NOT NULL CHECK (cantidad_entregada > 0),
    cantidad_devuelta INT NOT NULL DEFAULT 0 CHECK (cantidad_devuelta >= 0),
    cantidad_danada INT NOT NULL DEFAULT 0 CHECK (cantidad_danada >= 0),
    cantidad_perdida INT NOT NULL DEFAULT 0 CHECK (cantidad_perdida >= 0),
    estado_retorno VARCHAR(25) NOT NULL,
    observacion_novedad TEXT
);

CREATE TABLE movimientos_inventario (
    id UUID PRIMARY KEY,
    implemento_id UUID NOT NULL REFERENCES implementos(id),
    prestamo_id UUID REFERENCES prestamos(id),
    tipo_movimiento VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_nuevo INT NOT NULL,
    usuario_id UUID NOT NULL,
    observacion VARCHAR(255) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

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

CREATE INDEX idx_prestamos_sesion ON prestamos(sesion_juego_id);
CREATE INDEX idx_kardex_implemento ON movimientos_inventario(implemento_id);
