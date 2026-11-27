CREATE TABLE categorias_implemento (
    id UUID PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE implementos (
    id UUID PRIMARY KEY,
    categoria_id UUID NOT NULL REFERENCES categorias_implemento(id),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    stock_total INT NOT NULL CHECK (stock_total >= 0),
    stock_disponible INT NOT NULL CHECK (stock_disponible >= 0),
    stock_mantenimiento INT NOT NULL DEFAULT 0 CHECK (stock_mantenimiento >= 0),
    codigo_tarifa VARCHAR(60) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
