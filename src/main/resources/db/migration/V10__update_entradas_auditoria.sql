-- V10__update_entradas_auditoria.sql
ALTER TABLE entradas CHANGE COLUMN qr_code codigo_qr VARCHAR(255) NOT NULL UNIQUE;
ALTER TABLE entradas CHANGE COLUMN fecha_uso fecha_ingreso TIMESTAMP NULL;
ALTER TABLE entradas ADD COLUMN validado_por_id BIGINT;
ALTER TABLE entradas ADD CONSTRAINT fk_entrada_portero FOREIGN KEY (validado_por_id) REFERENCES usuarios(id);
