-- ============================================================
-- 1. Insertamos los vehiculos
-- ============================================================

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, inhabilitado, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (1, true, false, 2023, 'rojo', 'DISPONIBLE', 'Toyota', 'Corolla', 'AD123ZZ', 50000.00 , 'SEDAN');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, inhabilitado, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (2, true, false, 2024, 'blanco', 'DISPONIBLE', 'Ford', 'Ranger', 'AF456XY', 60000.00 , 'SUV');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, inhabilitado, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (3, true, false, 2021, 'plateado', 'DISPONIBLE', 'Volkswagen', 'Gol Trend', 'AF123CC', 40000.00 , 'SEDAN');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, inhabilitado, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (4, true, false, 2022, 'negro', 'DISPONIBLE', 'Honda', 'CR-V', 'AG789WX', 130000.00 , 'SUV');

INSERT IGNORE INTO vehiculos (id_vehiculo, activo, inhabilitado, anio, color, estado, marca, modelo, patente, precio_diario, tipo_vehiculo)
VALUES (5, true, false, 2020, 'blanco', 'DISPONIBLE', 'Renault', 'Sandero', 'AH123YZ', 40000.00 , 'HATCHBACK');