-- ==========================================
-- V2: COMPRAS Y ENTRADAS Flujo de compra + tickets
-- ==========================================

CREATE TABLE compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT fk_compra_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);

CREATE TABLE entradas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    compra_id BIGINT NOT NULL,
    tipo_entrada_id BIGINT NOT NULL,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    fecha_uso TIMESTAMP NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_entrada_evento
        FOREIGN KEY (evento_id) REFERENCES eventos(id),

    CONSTRAINT fk_entrada_compra
        FOREIGN KEY (compra_id) REFERENCES compras(id),

    CONSTRAINT fk_entrada_tipo
        FOREIGN KEY (tipo_entrada_id) REFERENCES tipos_entrada(id)
);