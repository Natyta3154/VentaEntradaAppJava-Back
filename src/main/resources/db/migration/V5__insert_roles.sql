-- ==========================================
-- MIGRACIÓN V2: Inserción de datos iniciales
-- ==========================================
-- Este script es ejecutado por Flyway de forma automática después de la V1.
-- Su propósito es "sembrar" (seed) la base de datos con los roles por defecto
-- que el sistema requiere para funcionar (Administrador y Cliente).
-- Es necesario porque el servicio de registro busca el ROLE_CLIENTE en la DB.

INSERT INTO roles(nombre)
VALUES ('ROLE_ADMIN');

INSERT INTO roles(nombre)
VALUES ('ROLE_CLIENTE');