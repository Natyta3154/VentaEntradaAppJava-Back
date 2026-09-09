-- ==========================================
-- V8: REFACTOR DJ TO ARTISTA
-- ==========================================

-- Renombrar tabla djs a artistas
RENAME TABLE djs TO artistas;

-- Renombrar tabla evento_dj a evento_artista
RENAME TABLE evento_dj TO evento_artista;

-- Renombrar la columna dj_id a artista_id en la tabla evento_artista
ALTER TABLE evento_artista CHANGE dj_id artista_id BIGINT NOT NULL;
