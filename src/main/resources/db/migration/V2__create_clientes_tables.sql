-- V2: Módulo de Clientes (RF-CL-001 a RF-CL-005)

CREATE SCHEMA IF NOT EXISTS clientes;

CREATE TABLE clientes.clientes (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id          UUID NOT NULL UNIQUE REFERENCES public.usuarios(id),
    nombre_completo     VARCHAR(150) NOT NULL,
    correo_electronico  VARCHAR(150) NOT NULL UNIQUE,
    telefono            VARCHAR(15) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE clientes.direcciones (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cliente_id          UUID NOT NULL REFERENCES clientes.clientes(id) ON DELETE CASCADE,
    alias               VARCHAR(50),
    calle               VARCHAR(150) NOT NULL,
    numero_exterior     VARCHAR(20) NOT NULL,
    numero_interior     VARCHAR(20),
    colonia             VARCHAR(100) NOT NULL,
    zona                VARCHAR(30) NOT NULL,
    codigo_postal       VARCHAR(5) NOT NULL,
    referencias         VARCHAR(300),
    ubicacion           GEOMETRY(Point, 4326),
    validada            BOOLEAN NOT NULL DEFAULT FALSE,
    es_predeterminada   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_direcciones_cliente_id ON clientes.direcciones(cliente_id);
CREATE INDEX idx_direcciones_zona ON clientes.direcciones(zona);
CREATE INDEX idx_direcciones_ubicacion ON clientes.direcciones USING GIST(ubicacion);

CREATE UNIQUE INDEX idx_direccion_predeterminada_unica
    ON clientes.direcciones(cliente_id)
    WHERE es_predeterminada = TRUE;
