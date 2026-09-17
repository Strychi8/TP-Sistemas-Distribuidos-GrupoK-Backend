-- 1. Insertamos los roles base (INSERT IGNORE evita errores si ya existen al reiniciar la app)
INSERT IGNORE INTO roles (nombre_rol) VALUES ('ADMINISTRADOR');
INSERT IGNORE INTO roles (nombre_rol) VALUES ('CLIENTE');

-- 2. Insertamos el usuario administrador
INSERT IGNORE INTO usuarios (email, password, activo) VALUES ('admin@empresarentar.com', 'admin123', true);

-- 3. Vinculamos el usuario (ID 1) con el rol ADMINISTRADOR (ID 1)
INSERT IGNORE INTO usuario_rol (usuario_id, rol_id, fecha_asignacion) VALUES (1, 1, NOW());

-- ============================================================
-- 4. Insertamos los vehiculos
-- ============================================================

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (1, true, 2023, 'rojo', 'DISPONIBLE', 'Toyota', 'Corolla', 'AD123ZZ', 50000.00 , 'SEDAN');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (2, true, 2024, 'blanco', 'DISPONIBLE', 'Ford', 'Ranger', 'AF456XY', 60000.00 , 'SUV');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (3, true, 2021, 'plateado', 'DISPONIBLE', 'Volkswagen', 'Gol Trend', 'AF123CC', 40000.00 , 'SEDAN');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (4, true, 2022, 'negro', 'DISPONIBLE', 'Honda', 'CR-V', 'AG789WX', 130000.00 , 'SUV');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (5, true, 2020, 'blanco', 'DISPONIBLE', 'Renault', 'Sandero', 'AH123YZ', 40000.00 , 'HATCHBACK');
