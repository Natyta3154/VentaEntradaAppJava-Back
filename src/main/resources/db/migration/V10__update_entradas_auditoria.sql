-- V10__update_entradas_auditoria.sql
ALTER TABLE entradas RENAME COLUMN qr_code TO codigo_qr;
ALTER TABLE entradas RENAME COLUMN fecha_uso TO fecha_ingreso;
ALTER TABLE entradas ADD COLUMN validado_por_id BIGINT;
ALTER TABLE entradas ADD CONSTRAINT fk_entrada_portero FOREIGN KEY (validado_por_id) REFERENCES usuarios(id);
