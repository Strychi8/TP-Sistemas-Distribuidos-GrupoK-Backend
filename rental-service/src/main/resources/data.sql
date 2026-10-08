-- ============================================================
-- Insertamos las reservas
--
-- importe_total = cantidad de días * precio_diario_historico
--
-- Vehículos:
-- 1 - Toyota Corolla      - $50.000/día
-- 2 - Ford Ranger         - $60.000/día
-- 3 - Volkswagen Gol      - $40.000/día
-- 4 - Honda CR-V          - $130.000/día
-- 5 - Renault Sandero     - $40.000/día
-- ============================================================


-- ============================================================
-- RESERVA 1
-- Juan Perez
-- Toyota Corolla
-- 3 días x $50.000 = $150.000
-- Estado: FINALIZADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (1, 1,
     '2026-09-01 10:00:00',
     '2026-09-04 10:00:00',
     150000.00,
     50000.00,
     'FINALIZADA',
     NOW());


-- ============================================================
-- RESERVA 2
-- Maria Gomez
-- Ford Ranger
-- 5 días x $60.000 = $300.000
-- Estado: FINALIZADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (2, 2,
     '2026-09-05 09:00:00',
     '2026-09-10 09:00:00',
     300000.00,
     60000.00,
     'FINALIZADA',
     NOW());


-- ============================================================
-- RESERVA 3
-- Lucas Fernandez
-- Volkswagen Gol Trend
-- 2 días x $40.000 = $80.000
-- Estado: CANCELADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (3, 3,
     '2026-09-12 14:00:00',
     '2026-09-14 14:00:00',
     80000.00,
     40000.00,
     'CANCELADA',
     NOW());


-- ============================================================
-- RESERVA 4
-- Ana Martinez
-- Honda CR-V
-- 4 días x $130.000 = $520.000
-- Estado: FINALIZADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (4, 4,
     '2026-09-15 11:00:00',
     '2026-09-19 11:00:00',
     520000.00,
     130000.00,
     'FINALIZADA',
     NOW());


-- ============================================================
-- RESERVA 5
-- Carlos Lopez
-- Renault Sandero
-- 3 días x $40.000 = $120.000
-- Estado: FINALIZADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (5, 5,
     '2026-09-20 08:00:00',
     '2026-09-23 08:00:00',
     120000.00,
     40000.00,
     'FINALIZADA',
     NOW());


-- ============================================================
-- RESERVA 6
-- Juan Perez
-- Ford Ranger
-- 4 días x $60.000 = $240.000
-- Estado: CONFIRMADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (1, 2,
     '2026-10-10 10:00:00',
     '2026-10-14 10:00:00',
     240000.00,
     60000.00,
     'CONFIRMADA',
     NOW());


-- ============================================================
-- RESERVA 7
-- Maria Gomez
-- Toyota Corolla
-- 7 días x $50.000 = $350.000
-- Estado: CONFIRMADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (2, 1,
     '2026-10-15 09:00:00',
     '2026-10-22 09:00:00',
     350000.00,
     50000.00,
     'CONFIRMADA',
     NOW());


-- ============================================================
-- RESERVA 8
-- Lucas Fernandez
-- Honda CR-V
-- 3 días x $130.000 = $390.000
-- Estado: CONFIRMADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (3, 4,
     '2026-10-20 12:00:00',
     '2026-10-23 12:00:00',
     390000.00,
     130000.00,
     'CONFIRMADA',
     NOW());


-- ============================================================
-- RESERVA 9
-- Ana Martinez
-- Volkswagen Gol Trend
-- 5 días x $40.000 = $200.000
-- Estado: CONFIRMADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (4, 3,
     '2026-10-25 10:00:00',
     '2026-10-30 10:00:00',
     200000.00,
     40000.00,
     'CONFIRMADA',
     NOW());


-- ============================================================
-- RESERVA 10
-- Carlos Lopez
-- Renault Sandero
-- 2 días x $40.000 = $80.000
-- Estado: CONFIRMADA
-- ============================================================

INSERT INTO reservas
(cliente_id, vehiculo_id, fecha_inicio, fecha_fin,
 importe_total, precio_diario_historico, estado, fecha_creacion)
VALUES
    (5, 5,
     '2026-10-28 14:00:00',
     '2026-10-30 14:00:00',
     80000.00,
     40000.00,
     'CONFIRMADA',
     NOW());