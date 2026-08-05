-- 0. Limpiar datos
TRUNCATE TABLE ubicaciones, pedidos, repartidores CASCADE;
TRUNCATE TABLE ubicaciones, pedidos, repartidores, rutas CASCADE;
-- 1. Insertar Repartidores (Ahora con correo y contraseña, respetando tu estado DISPONIBLE inicial)
INSERT INTO repartidores (id, nombre_completo, estado, placa_vehiculo, telefono, correo, contrasena)
VALUES (6, 'Carlos Sánchez', 'DISPONIBLE', 'NMP-2026', '5512345678', 'carlos@correo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjQsQnEsg2'),
       (4, 'Luis Fernando', 'DISPONIBLE', 'RUT-9988', '5598765432', 'luis@correo.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjQsQnEsg2');

-- 2. Insertar Pedidos (Ahora con coordenadas, fecha, teléfono y el id del repartidor asignado)
INSERT INTO pedidos (id, direccion_destino, latitud_destino, longitud_destino, fecha_creacion, telefono_cliente, estado, repartidor_id)
VALUES (1001, 'Av. Juárez Sur, Texcoco Centro', 19.5111, -98.8811, CURRENT_TIMESTAMP, '5500000001', 'PAGADO', 6),
       (1002, 'Avenida del Trabajo, Chiautla', 19.5444, -98.8855, CURRENT_TIMESTAMP, '5500000002', 'PAGADO', 4);

-- 3. Insertar Ubicaciones (Esto se queda igual)
INSERT INTO ubicaciones (id, fecha_hora, latitud, longitud, repartidor_id)
VALUES (1, CURRENT_TIMESTAMP, 19.5135, -98.8798, 6),
       (2, CURRENT_TIMESTAMP, 19.5452, -98.8872, 4);