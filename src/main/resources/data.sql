-- 0. Limpiar datos
TRUNCATE TABLE ubicaciones, test.pedidos, repartidores, rutas CASCADE;

-- 1. Insertar Repartidores
INSERT INTO repartidores (id, nombre_completo, estado, placa_vehiculo, telefono, correo, contrasena)
VALUES (6, 'Carlos Sánchez', 'DISPONIBLE', 'NMP-2026', '5512345678', 'carlos@correo.com', '$2a$10$EixZaYVK1fsbw1ZfbX30XePaWxn96p36WQo...'),
       (4, 'Luis Fernando', 'DISPONIBLE', 'RUT-9988', '5598765432', 'luis@correo.com', '$2a$10$EixZaYVK1fsbw1ZfbX30XePaWxn96p36WQo...');

-- 2. Insertar Pedidos
-- Usamos UUIDs fijos para el ID del pedido, e IDs aleatorios para el cliente y dirección.
INSERT INTO test.pedidos (
    id, numero_pedido, cliente_id, direccion_entrega_id,
    estado, subtotal, iva, total
) VALUES
      (
          '550e8400-e29b-41d4-a716-446655440001', -- <- USAR ESTE UUID EN .HTTP (Pedido 1)
          'PED-TEX-001',
          gen_random_uuid(), -- Simula un cliente
          gen_random_uuid(), -- Simula una dirección en Texcoco
          'CONFIRMADO',
          100.00, 16.00, 116.00
      ),
      (
          '550e8400-e29b-41d4-a716-446655440002', -- <- USAR ESTE UUID EN .HTTP (Pedido 1)
          'PED-CHI-002',
          gen_random_uuid(),
          gen_random_uuid(), -- Simula una dirección en Chiautla
          'CONFIRMADO',
          200.00, 32.00, 232.00
      );

-- 3. Insertar Ubicaciones }
INSERT INTO ubicaciones (id, fecha_hora, latitud, longitud, repartidor_id)
VALUES (1, CURRENT_TIMESTAMP, 19.5135, -98.8798, 6),
       (2, CURRENT_TIMESTAMP, 19.5452, -98.8872, 4);