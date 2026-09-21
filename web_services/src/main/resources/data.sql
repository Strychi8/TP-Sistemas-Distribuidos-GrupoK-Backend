-- ============================================================
-- 1. Insertamos los roles base
-- INSERT IGNORE evita errores si los roles ya existen
-- ============================================================

INSERT IGNORE INTO roles (nombre_rol)
VALUES ('ADMINISTRADOR');

INSERT IGNORE INTO roles (nombre_rol)
VALUES ('CLIENTE');


-- ============================================================
-- 2. Insertamos los usuarios
-- Los primeros 5 usuarios corresponden a clientes
-- El usuario 6 corresponde al administrador
-- ============================================================

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('juan.perez@gmail.com', '$2a$10$6IPVfWABzMXZtj4OSzWPYeIH7p0WXz1o1leB2zrhAY/ZM96PxEp.u', true);

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('maria.gomez@gmail.com', '$2a$10$6IPVfWABzMXZtj4OSzWPYeIH7p0WXz1o1leB2zrhAY/ZM96PxEp.u', true);

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('lucas.fernandez@gmail.com', '$2a$10$6IPVfWABzMXZtj4OSzWPYeIH7p0WXz1o1leB2zrhAY/ZM96PxEp.u', true);

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('ana.martinez@gmail.com', '$2a$10$6IPVfWABzMXZtj4OSzWPYeIH7p0WXz1o1leB2zrhAY/ZM96PxEp.u', true);

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('carlos.lopez@gmail.com', '$2a$10$6IPVfWABzMXZtj4OSzWPYeIH7p0WXz1o1leB2zrhAY/ZM96PxEp.u', true);

INSERT IGNORE INTO usuarios (email, password, activo)
VALUES ('admin@empresarentar.com', '$2a$10$Vr6r0XMtisV0nQ1Dl4P.UONTi1lKoZYi9kwI6S5nVDszX5n.hTdlm', true);


-- ============================================================
-- 3. Insertamos los clientes
-- Cada cliente se vincula con su usuario correspondiente
-- ============================================================

INSERT IGNORE INTO clientes
    (usuario_id, dni, nombre, apellido, email, telefono, fecha_nacimiento, activo)
VALUES
    (1, '40111222', 'Juan', 'Perez', 'juan.perez@gmail.com',
     '1123456789', '1995-03-15', true);

INSERT IGNORE INTO clientes
    (usuario_id, dni, nombre, apellido, email, telefono, fecha_nacimiento, activo)
VALUES
    (2, '41222333', 'Maria', 'Gomez', 'maria.gomez@gmail.com',
     '1134567890', '1997-07-22', true);

INSERT IGNORE INTO clientes
    (usuario_id, dni, nombre, apellido, email, telefono, fecha_nacimiento, activo)
VALUES
    (3, '42333444', 'Lucas', 'Fernandez', 'lucas.fernandez@gmail.com',
     '1145678901', '1992-11-08', true);

INSERT IGNORE INTO clientes
    (usuario_id, dni, nombre, apellido, email, telefono, fecha_nacimiento, activo)
VALUES
    (4, '43444555', 'Ana', 'Martinez', 'ana.martinez@gmail.com',
     '1156789012', '1999-01-30', true);

INSERT IGNORE INTO clientes
    (usuario_id, dni, nombre, apellido, email, telefono, fecha_nacimiento, activo)
VALUES
    (5, '44555666', 'Carlos', 'Lopez', 'carlos.lopez@gmail.com',
     '1167890123', '1990-09-17', true);


-- ============================================================
-- 4. Asignamos el rol CLIENTE a los usuarios de los clientes
-- El rol CLIENTE corresponde al ID 2
-- ============================================================

INSERT IGNORE INTO usuario_rol
    (usuario_id, rol_id, fecha_asignacion)
VALUES
    (1, 2, NOW()),
    (2, 2, NOW()),
    (3, 2, NOW()),
    (4, 2, NOW()),
    (5, 2, NOW());


-- ============================================================
-- 5. Asignamos el rol ADMINISTRADOR al usuario administrador
-- El usuario administrador corresponde al ID 6
-- El rol ADMINISTRADOR corresponde al ID 1
-- ============================================================

INSERT IGNORE INTO usuario_rol
    (usuario_id, rol_id, fecha_asignacion)
VALUES
    (6, 1, NOW());

-- ============================================================
-- 6. Insertamos los vehiculos
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