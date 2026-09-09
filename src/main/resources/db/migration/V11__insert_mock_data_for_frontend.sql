-- ==========================================
-- V11: INSERT MOCK DATA FOR FRONTEND
-- ==========================================

-- 1. Insertar Artistas
INSERT INTO artistas (nombre, estilo, descripcion) VALUES 
('David Guetta', 'EDM', 'Legendario DJ y productor de música electrónica mundial.'),
('Foo Fighters', 'Rock Alternativo', 'Banda de rock estadounidense liderada por Dave Grohl.'),
('Bizarrap', 'Trap / Urbano', 'Productor musical argentino reconocido a nivel mundial.');

-- 2. Insertar Eventos
INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, estado, edad_minima, fecha_fin) VALUES 
('Tomorrowland Mítico', 'El mejor festival de música electrónica llega a la ciudad. Un escenario nunca antes visto.', '2026-10-15 20:00:00', 'Estadio Central', 50000, 'ACTIVO', 18, '2026-10-16 05:00:00'),
('Noche de Rock', 'Una noche inolvidable con las mejores bandas de rock en vivo. Pura energía.', '2026-11-20 21:00:00', 'Teatro Gran Rex', 5000, 'ACTIVO', 14, '2026-11-21 00:00:00'),
('Sunset Session', 'Despide el sol con la mejor música frente al mar.', '2026-12-05 17:00:00', 'Playa Sur', 2000, 'ACTIVO', 18, '2026-12-05 23:59:00');

-- 3. Relacionar Artistas con Eventos
-- Tomorrowland (1) -> David Guetta (1)
INSERT INTO evento_artista (evento_id, artista_id) VALUES (1, 1);
-- Noche de Rock (2) -> Foo Fighters (2)
INSERT INTO evento_artista (evento_id, artista_id) VALUES (2, 2);
-- Sunset Session (3) -> Bizarrap (3)
INSERT INTO evento_artista (evento_id, artista_id) VALUES (3, 3);

-- 4. Insertar Imágenes de Eventos (URLs reales de Unsplash para estética Premium)
INSERT INTO evento_imagenes (evento_id, nombre_imagen, url_imagen, orden) VALUES 
(1, 'Portada Tomorrowland', 'https://images.unsplash.com/photo-1540039155732-684735035726?auto=format&fit=crop&w=1200&q=80', 1),
(2, 'Portada Rock', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=1200&q=80', 1),
(3, 'Portada Sunset', 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80', 1);

-- 5. Insertar Tipos de Entrada
-- Tomorrowland (1)
INSERT INTO tipos_entrada (evento_id, nombre, precio, stock_total, stock_disponible, fecha_inicio_venta, fecha_fin_venta) VALUES 
(1, 'General', 15000.00, 40000, 40000, '2026-06-01 00:00:00', '2026-10-15 19:00:00'),
(1, 'VIP', 35000.00, 10000, 10000, '2026-06-01 00:00:00', '2026-10-15 19:00:00');

-- Noche de Rock (2)
INSERT INTO tipos_entrada (evento_id, nombre, precio, stock_total, stock_disponible, fecha_inicio_venta, fecha_fin_venta) VALUES 
(2, 'Platea', 20000.00, 3000, 3000, '2026-06-01 00:00:00', '2026-11-20 20:00:00'),
(2, 'Campo', 12000.00, 2000, 2000, '2026-06-01 00:00:00', '2026-11-20 20:00:00');

-- Sunset Session (3)
INSERT INTO tipos_entrada (evento_id, nombre, precio, stock_total, stock_disponible, fecha_inicio_venta, fecha_fin_venta) VALUES 
(3, 'Acceso Único', 8000.00, 2000, 2000, '2026-06-01 00:00:00', '2026-12-05 16:00:00');
