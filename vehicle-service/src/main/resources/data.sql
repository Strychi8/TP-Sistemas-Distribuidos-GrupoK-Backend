-- ============================================================
-- 1. Insertamos los vehiculos
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