-- ==========================================
-- V9: AÑADIR SOFT DELETES (BORRADO LÓGICO)
-- ==========================================

ALTER TABLE usuarios ADD COLUMN activo BOOLEAN DEFAULT TRUE;
ALTER TABLE eventos ADD COLUMN activo BOOLEAN DEFAULT TRUE;
ALTER TABLE artistas ADD COLUMN activo BOOLEAN DEFAULT TRUE;
ALTER TABLE evento_imagenes ADD COLUMN activo BOOLEAN DEFAULT TRUE;
ALTER TABLE evento_videos ADD COLUMN activo BOOLEAN DEFAULT TRUE;
