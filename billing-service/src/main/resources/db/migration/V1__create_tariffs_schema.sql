CREATE TABLE tarifas_cancha (
    id UUID PRIMARY KEY,
    tipo_cancha VARCHAR(30) NOT NULL,
    franja VARCHAR(20) NOT NULL,
    valor_hora_cop NUMERIC(12,2) NOT NULL CHECK (valor_hora_cop >= 0),
    vigente_desde TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    vigente_hasta TIMESTAMPTZ,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por_id UUID,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tarifas_implemento (
    id UUID PRIMARY KEY,
    codigo_tarifa VARCHAR(60) NOT NULL,
    nombre_referencia VARCHAR(120) NOT NULL,
    valor_alquiler_cop NUMERIC(12,2) NOT NULL CHECK (valor_alquiler_cop >= 0),
    valor_reposicion_cop NUMERIC(12,2) NOT NULL CHECK (valor_reposicion_cop >= 0),
    vigente_desde TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    vigente_hasta TIMESTAMPTZ,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por_id UUID,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_tarifa_cancha_activa ON tarifas_cancha (tipo_cancha, franja) WHERE activa = TRUE;
CREATE UNIQUE INDEX uk_tarifa_implemento_activa ON tarifas_implemento (codigo_tarifa) WHERE activa = TRUE;
