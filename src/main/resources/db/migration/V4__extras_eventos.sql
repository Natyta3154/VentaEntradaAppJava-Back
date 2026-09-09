-- ==========================================
-- V4: EXTRAS DE EVENTOS (MEDIA Y DJS)
-- ==========================================

CREATE TABLE djs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    estilo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE evento_dj (
    evento_id BIGINT NOT NULL,
    dj_id BIGINT NOT NULL,

    PRIMARY KEY (evento_id, dj_id),

    FOREIGN KEY (evento_id) REFERENCES eventos(id),
    FOREIGN KEY (dj_id) REFERENCES djs(id)
);

CREATE TABLE evento_imagenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    nombre_imagen VARCHAR(55) NOT NULL,
    url_imagen VARCHAR(255) NOT NULL,
    orden INT DEFAULT 0,

    FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE CASCADE
);

CREATE TABLE evento_videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    nombre_video VARCHAR(55) NOT NULL,
    url_video VARCHAR(255) NOT NULL,
    orden INT DEFAULT 0,

    FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE CASCADE
);