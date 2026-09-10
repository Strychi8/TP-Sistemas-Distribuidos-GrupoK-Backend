-- 1. Insertamos los roles base (INSERT IGNORE evita errores si ya existen al reiniciar la app)
INSERT IGNORE INTO roles (nombre_rol) VALUES ('ADMINISTRADOR');
INSERT IGNORE INTO roles (nombre_rol) VALUES ('CLIENTE');

-- 2. Insertamos el usuario administrador
INSERT IGNORE INTO usuarios (email, password, activo) VALUES ('admin@empresarentar.com', 'admin123', true);

-- 3. Vinculamos el usuario (ID 1) con el rol ADMINISTRADOR (ID 1)
INSERT IGNORE INTO usuario_rol (usuario_id, rol_id, fecha_asignacion) VALUES (1, 1, NOW());
