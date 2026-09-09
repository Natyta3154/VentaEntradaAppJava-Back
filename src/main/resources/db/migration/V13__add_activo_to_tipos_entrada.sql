-- Agregar columna activo a la tabla tipos_entrada (Soft Delete)
ALTER TABLE tipos_entrada ADD COLUMN activo BOOLEAN DEFAULT TRUE;

-- Actualizar registros existentes para que sean activos por defecto
UPDATE tipos_entrada SET activo = TRUE WHERE activo IS NULL;
