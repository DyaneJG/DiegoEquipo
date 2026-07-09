-- ==========================================================
-- EXTENSIONES
-- ==========================================================
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================================
-- DOMINIO: AUTENTICACION Y RBAC
-- ==========================================================
CREATE TABLE roles (
    id     SMALLINT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (id, nombre) VALUES
    (1, 'admin'),
    (2, 'supervisor'),
    (3, 'repartidor'),
    (4, 'cliente');

CREATE TABLE usuarios (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_completo VARCHAR(150) NOT NULL,
    correo          VARCHAR(150) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    rol_id          SMALLINT NOT NULL REFERENCES roles(id),
    mfa_habilitado  BOOLEAN NOT NULL DEFAULT FALSE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usuarios_rol ON usuarios(rol_id);

-- ==========================================================
-- DOMINIO: CLIENTES
-- ==========================================================
CREATE TABLE clientes (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    creado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==========================================================
-- DOMINIO: ZONAS (geocercas)
-- ==========================================================
CREATE TABLE zonas (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    poligono    GEOMETRY(Polygon, 4326) NOT NULL,
    color_hex   CHAR(7),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_zonas_poligono ON zonas USING GIST(poligono);

-- ==========================================================
-- DOMINIO: DIRECCIONES CLIENTE (con zona automática)
-- ==========================================================
CREATE TABLE direcciones_cliente (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cliente_id        UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    etiqueta          VARCHAR(50),
    calle_numero      VARCHAR(200) NOT NULL,
    colonia           VARCHAR(120),
    municipio         VARCHAR(80) NOT NULL,
    codigo_postal     VARCHAR(10) NOT NULL,
    referencias       TEXT,
    ubicacion         GEOMETRY(Point, 4326) NOT NULL,
    zona_id           UUID REFERENCES zonas(id) ON DELETE SET NULL,
    es_predeterminada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_direcciones_cliente ON direcciones_cliente(cliente_id);
CREATE INDEX idx_direcciones_ubicacion ON direcciones_cliente USING GIST(ubicacion);
CREATE INDEX idx_direcciones_zona ON direcciones_cliente(zona_id);

-- Trigger para asignar zona automáticamente
CREATE OR REPLACE FUNCTION asignar_zona_automatica()
RETURNS TRIGGER AS $$
BEGIN
    SELECT id INTO NEW.zona_id
    FROM zonas
    WHERE ST_Within(NEW.ubicacion, poligono)
    LIMIT 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_asignar_zona
BEFORE INSERT OR UPDATE OF ubicacion ON direcciones_cliente
FOR EACH ROW
EXECUTE FUNCTION asignar_zona_automatica();

-- ==========================================================
-- DOMINIO: CATALOGO (productos e inventario separados)
-- ==========================================================
CREATE TABLE categorias (
    id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku             VARCHAR(50) NOT NULL UNIQUE,
    nombre          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    categoria_id    UUID REFERENCES categorias(id),
    imagen_url      VARCHAR(500),
    unidad_medida   VARCHAR(20) NOT NULL DEFAULT 'pieza',
    peso_kg         NUMERIC(8,3),
    volumen_m3      NUMERIC(8,4),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_productos_categoria ON productos(categoria_id);

CREATE TABLE inventario (
    id                  UUID PRIMARY KEY REFERENCES productos(id) ON DELETE CASCADE,
    stock_actual        INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo        INTEGER NOT NULL DEFAULT 5,
    stock_reservado     INTEGER NOT NULL DEFAULT 0 CHECK (stock_reservado >= 0),
    reserva_expira_at   TIMESTAMPTZ,
    ultima_actualizacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventario_stock_bajo ON inventario(id) WHERE stock_actual <= stock_minimo;

-- ==========================================================
-- DOMINIO: TASAS DE IMPUESTOS (históricas)
-- ==========================================================
CREATE TABLE tasas_impuestos (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre        VARCHAR(50) NOT NULL,
    porcentaje    NUMERIC(5,4) NOT NULL CHECK (porcentaje >= 0),
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO tasas_impuestos (nombre, porcentaje, vigencia_desde)
VALUES ('IVA General', 0.1600, '2020-01-01');

-- ==========================================================
-- DOMINIO: VEHICULOS
-- ==========================================================
CREATE TABLE vehiculos (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo                VARCHAR(50) NOT NULL,
    placa               VARCHAR(15) NOT NULL UNIQUE,
    capacidad_peso_kg   NUMERIC(8,2) NOT NULL,
    capacidad_volumen_m3 NUMERIC(8,3) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==========================================================
-- DOMINIO: REPARTIDORES
-- ==========================================================
CREATE TABLE repartidores (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    vehiculo_actual_id  UUID REFERENCES vehiculos(id) ON DELETE SET NULL,
    zona_preferente_id  UUID REFERENCES zonas(id) ON DELETE SET NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'FUERA_DE_LINEA',
    fcm_token           TEXT,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_repartidores_usuario ON repartidores(usuario_id);

-- ==========================================================
-- DOMINIO: PEDIDOS
-- ==========================================================
CREATE TABLE pedidos (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_pedido           VARCHAR(25) NOT NULL UNIQUE,
    cliente_id              UUID NOT NULL REFERENCES clientes(id),
    direccion_entrega_id    UUID NOT NULL REFERENCES direcciones_cliente(id),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    subtotal                NUMERIC(12,2) NOT NULL,
    iva                     NUMERIC(12,2) NOT NULL,
    cargo_entrega           NUMERIC(8,2) NOT NULL DEFAULT 0,
    total                   NUMERIC(12,2) NOT NULL,
    notas                   TEXT,
    ventana_inicio          TIME,
    ventana_fin             TIME,
    fecha_entrega_programada DATE,
    fecha_entrega_real      TIMESTAMPTZ,
    cancelado_motivo        TEXT,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedidos_cliente ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha_programada ON pedidos(fecha_entrega_programada);

CREATE TABLE pedido_detalle (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id           UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id         UUID NOT NULL REFERENCES productos(id),
    cantidad            INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario     NUMERIC(12,2) NOT NULL,
    tasa_iva_aplicada   NUMERIC(5,4) NOT NULL,
    iva_unitario        NUMERIC(12,2) NOT NULL,
    subtotal_linea      NUMERIC(12,2) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedido_detalle_pedido ON pedido_detalle(pedido_id);
CREATE INDEX idx_pedido_detalle_producto ON pedido_detalle(producto_id);

-- ==========================================================
-- DOMINIO: PAGOS
-- ==========================================================
CREATE TABLE metodos_pago (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo        VARCHAR(30) NOT NULL UNIQUE,
    nombre        VARCHAR(100) NOT NULL,
    requiere_token BOOLEAN NOT NULL DEFAULT FALSE,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO metodos_pago (codigo, nombre) VALUES
    ('efectivo', 'Efectivo'),
    ('stripe', 'Tarjeta (Stripe)'),
    ('mercadopago', 'Tarjeta (Mercado Pago)'),
    ('spei', 'Transferencia SPEI'),
    ('codi', 'CoDi');

CREATE TABLE pagos (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id          UUID NOT NULL REFERENCES pedidos(id) ON DELETE RESTRICT,
    metodo_pago_id     UUID NOT NULL REFERENCES metodos_pago(id),
    proveedor          VARCHAR(30),
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    monto              NUMERIC(12,2) NOT NULL,
    referencia_externa VARCHAR(200),
    qr_codigo          TEXT,
    qr_expira_at       TIMESTAMPTZ,
    pagado_at          TIMESTAMPTZ,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_pagos_pedido ON pagos(pedido_id);
CREATE INDEX idx_pagos_estado ON pagos(estado);

-- ==========================================================
-- DOMINIO: AUDITORIA DE ESTADOS DE PEDIDO
-- ==========================================================
CREATE TABLE pedido_estados_log (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id         UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    estado_anterior   VARCHAR(20),
    estado_nuevo      VARCHAR(20) NOT NULL,
    usuario_id        UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    repartidor_id     UUID REFERENCES repartidores(id) ON DELETE SET NULL,
    tipo_actor        VARCHAR(20) NOT NULL CHECK (tipo_actor IN ('admin', 'repartidor', 'sistema', 'cliente')),
    notas             TEXT,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedidos_log_pedido ON pedido_estados_log(pedido_id);
CREATE INDEX idx_pedidos_log_creado ON pedido_estados_log(creado_en);

-- ==========================================================
-- DOMINIO: RUTAS
-- ==========================================================
CREATE TABLE rutas (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_ruta         VARCHAR(20) NOT NULL UNIQUE,
    repartidor_id       UUID NOT NULL REFERENCES repartidores(id),
    vehiculo_id         UUID NOT NULL REFERENCES vehiculos(id),
    zona_id             UUID NOT NULL REFERENCES zonas(id),
    fecha_ruta          DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA',
    distancia_total_km  NUMERIC(8,2),
    tiempo_estimado_min INTEGER,
    total_paradas       SMALLINT NOT NULL DEFAULT 0,
    geometria_ruta      GEOMETRY(LineString, 4326),
    inicio_real_at      TIMESTAMPTZ,
    fin_real_at         TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rutas_repartidor ON rutas(repartidor_id);
CREATE INDEX idx_rutas_fecha ON rutas(fecha_ruta);
CREATE INDEX idx_rutas_estado ON rutas(estado);

-- ==========================================================
-- DOMINIO: RUTA PARADAS
-- ==========================================================
CREATE TABLE ruta_paradas (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ruta_id               UUID NOT NULL REFERENCES rutas(id) ON DELETE CASCADE,
    pedido_id             UUID NOT NULL REFERENCES pedidos(id) ON DELETE RESTRICT,
    secuencia             INTEGER NOT NULL CHECK (secuencia > 0),
    eta                   TIMESTAMPTZ,
    distancia_anterior_km NUMERIC(6,2),
    estado                VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_ruta_paradas_pedido ON ruta_paradas(pedido_id);
CREATE INDEX idx_ruta_paradas_ruta ON ruta_paradas(ruta_id, secuencia);

-- ==========================================================
-- DOMINIO: ENTREGAS
-- ==========================================================
CREATE TABLE entregas (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parada_id             UUID NOT NULL UNIQUE REFERENCES ruta_paradas(id) ON DELETE CASCADE,
    checkin_at            TIMESTAMPTZ,
    checkin_coordenadas   GEOMETRY(Point, 4326),
    checkout_at           TIMESTAMPTZ,
    entregado_at          TIMESTAMPTZ,
    confirmacion_tipo     VARCHAR(20) CHECK (confirmacion_tipo IN ('firma_digital', 'pin', 'foto')),
    pin_generado          CHAR(4),
    firma_url             TEXT,
    resultado             VARCHAR(30) CHECK (resultado IN ('entregado', 'no_entregado', 'parcial')),
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_entregas_parada ON entregas(parada_id);
CREATE INDEX idx_entregas_checkin_geom ON entregas USING GIST(checkin_coordenadas);

-- ==========================================================
-- DOMINIO: EVIDENCIAS DE ENTREGA
-- ==========================================================
CREATE TABLE entrega_evidencias (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entrega_id    UUID NOT NULL REFERENCES entregas(id) ON DELETE CASCADE,
    tipo          VARCHAR(30) NOT NULL CHECK (tipo IN ('entrega', 'incidencia', 'checkin', 'otro')),
    url           TEXT NOT NULL,
    coordenadas   GEOMETRY(Point, 4326),
    taken_at      TIMESTAMPTZ NOT NULL,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidencias_entrega ON entrega_evidencias(entrega_id);

-- ==========================================================
-- DOMINIO: INCIDENCIAS
-- ==========================================================
CREATE TABLE incidencias (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entrega_id         UUID NOT NULL REFERENCES entregas(id) ON DELETE RESTRICT,
    repartidor_id      UUID NOT NULL REFERENCES repartidores(id) ON DELETE RESTRICT,
    admin_id           UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    tipo               VARCHAR(50) NOT NULL,
    descripcion        TEXT NOT NULL,
    estado_resolucion  VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    resolucion_notas   TEXT,
    resuelto_at        TIMESTAMPTZ,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidencias_entrega ON incidencias(entrega_id);

-- ==========================================================
-- DOMINIO: TRACKING DE UBICACIONES (particionado por mes)
-- ==========================================================
CREATE TABLE tracking_ubicaciones (
    id            UUID NOT NULL DEFAULT uuid_generate_v4(),
    repartidor_id UUID NOT NULL REFERENCES repartidores(id) ON DELETE CASCADE,
    ubicacion     GEOMETRY(Point, 4326) NOT NULL,
    precision_metros NUMERIC(6,2),
    velocidad_kmh NUMERIC(6,2),
    rumbo_grados  NUMERIC(5,2),
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, creado_en)
) PARTITION BY RANGE (creado_en);

CREATE TABLE tracking_ubicaciones_2026_07 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE tracking_ubicaciones_2026_08 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE tracking_ubicaciones_2026_09 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE tracking_ubicaciones_2026_10 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

CREATE INDEX idx_tracking_repartidor ON tracking_ubicaciones(repartidor_id);
CREATE INDEX idx_tracking_ubicacion ON tracking_ubicaciones USING GIST(ubicacion);
CREATE INDEX idx_tracking_creado ON tracking_ubicaciones(creado_en DESC);

-- ==========================================================
-- DOMINIO: NOTIFICACIONES
-- ==========================================================
CREATE TABLE notificaciones (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id        UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    pedido_id         UUID REFERENCES pedidos(id) ON DELETE SET NULL,
    canal             VARCHAR(20) NOT NULL CHECK (canal IN ('push', 'email', 'sms', 'whatsapp')),
    titulo            VARCHAR(200) NOT NULL,
    cuerpo            TEXT NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'enviado', 'fallido', 'leido')),
    enviado_at        TIMESTAMPTZ,
    error_detalle     TEXT,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_pedido ON notificaciones(pedido_id) WHERE pedido_id IS NOT NULL;
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado);

-- ==========================================================
-- VISTA UTIL: pedidos con ultimo estado
-- ==========================================================
CREATE VIEW v_pedidos_actuales AS
SELECT 
    p.id,
    p.numero_pedido,
    u.nombre_completo AS cliente,
    p.estado,
    p.total,
    p.fecha_entrega_programada,
    p.creado_en,
    (SELECT creado_en FROM pedido_estados_log WHERE pedido_id = p.id ORDER BY creado_en DESC LIMIT 1) AS ultimo_cambio
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
JOIN usuarios u ON c.usuario_id = u.id;

-- ==========================================================
-- FIN DEL SCRIPT
-- ==========================================================
