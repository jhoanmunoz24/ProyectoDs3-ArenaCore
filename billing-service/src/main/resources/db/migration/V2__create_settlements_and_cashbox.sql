CREATE TABLE liquidaciones (
    id UUID PRIMARY KEY,
    sesion_juego_id UUID NOT NULL UNIQUE,
    cancha_nombre VARCHAR(120) NOT NULL,
    tipo_cancha VARCHAR(30) NOT NULL,
    abono_previo_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal_cancha_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal_implementos_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal_penalidades_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE detalles_liquidacion (
    id UUID PRIMARY KEY,
    liquidacion_id UUID NOT NULL REFERENCES liquidaciones(id) ON DELETE CASCADE,
    concepto VARCHAR(40) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    valor_unitario_snapshot_cop NUMERIC(12,2) NOT NULL,
    subtotal_cop NUMERIC(12,2) NOT NULL
);

CREATE TABLE cajas (
    id UUID PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sesiones_caja (
    id UUID PRIMARY KEY,
    caja_id UUID NOT NULL REFERENCES cajas(id),
    operador_id UUID NOT NULL,
    saldo_inicial_cop NUMERIC(12,2) NOT NULL CHECK (saldo_inicial_cop >= 0),
    total_efectivo_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_transferencia_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_datafono_cop NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_cierre_declarado_cop NUMERIC(12,2),
    diferencia_arqueo_cop NUMERIC(12,2),
    estado VARCHAR(20) NOT NULL,
    abierta_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cerrada_en TIMESTAMPTZ
);

CREATE TABLE pagos (
    id UUID PRIMARY KEY,
    liquidacion_id UUID NOT NULL REFERENCES liquidaciones(id),
    sesion_caja_id UUID NOT NULL REFERENCES sesiones_caja(id),
    metodo_pago VARCHAR(30) NOT NULL,
    referencia_transaccion VARCHAR(120),
    monto_cop NUMERIC(12,2) NOT NULL CHECK (monto_cop > 0),
    estado VARCHAR(20) NOT NULL,
    operador_id UUID NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE movimientos_caja (
    id UUID PRIMARY KEY,
    sesion_caja_id UUID NOT NULL REFERENCES sesiones_caja(id),
    pago_id UUID NOT NULL REFERENCES pagos(id),
    tipo VARCHAR(30) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    monto_cop NUMERIC(12,2) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE comprobantes (
    id UUID PRIMARY KEY,
    numero_comprobante VARCHAR(60) NOT NULL UNIQUE,
    pago_id UUID NOT NULL UNIQUE REFERENCES pagos(id),
    liquidacion_id UUID NOT NULL REFERENCES liquidaciones(id),
    detalle_json TEXT NOT NULL,
    total_pagado_cop NUMERIC(12,2) NOT NULL,
    emitido_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
