CREATE TABLE notificaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    pedido_id UUID REFERENCES pedidos(id),
    canal VARCHAR(20) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    cuerpo TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    enviado_at TIMESTAMPTZ,
    error_detalle TEXT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificaciones_usuario_id ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado);