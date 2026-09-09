-- V10__update_entradas_auditoria.sql
ALTER TABLE entradas
CHANGE COLUMN qr_code codigo_qr VARCHAR(255) NOT NULL UNIQUE,
CHANGE COLUMN fecha_uso fecha_ingreso TIMESTAMP NULL,
ADD COLUMN validado_por_id BIGINT,
ADD CONSTRAINT fk_entrada_portero FOREIGN KEY (validado_por_id) REFERENCES usuarios(id);
