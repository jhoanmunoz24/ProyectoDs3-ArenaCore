CREATE TABLE autorizaciones_excepcionales (
    id UUID PRIMARY KEY,
    tipo VARCHAR(60) NOT NULL,
    referencia_externa_id UUID NOT NULL,
    solicitado_por_id UUID NOT NULL REFERENCES usuarios(id),
    aprobado_por_id UUID REFERENCES usuarios(id),
    motivo TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    observacion_resolucion TEXT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resuelto_en TIMESTAMPTZ
);

CREATE TABLE auditoria_seguridad (
    id UUID PRIMARY KEY,
    usuario_id UUID,
    email_intento VARCHAR(150),
    evento VARCHAR(60) NOT NULL,
    detalle TEXT NOT NULL,
    ip_origen VARCHAR(80),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_usuario ON auditoria_seguridad(usuario_id);
CREATE INDEX idx_auditoria_fecha ON auditoria_seguridad(creado_en);
CREATE INDEX idx_autorizaciones_ref ON autorizaciones_excepcionales(referencia_externa_id);
