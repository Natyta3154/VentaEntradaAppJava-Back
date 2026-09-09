-- ==========================================
-- V3: PAGOS Y DEVOLUCIONES
-- ==========================================

CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    proveedor VARCHAR(50) NOT NULL,
    referencia_pago VARCHAR(255),
    monto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    medio_pago VARCHAR(50),
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (compra_id)
        REFERENCES compras(id)
);

CREATE TABLE devoluciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pago_id BIGINT NOT NULL,
    motivo VARCHAR(255),
    monto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    referencia_reembolso VARCHAR(255),
    fecha_devolucion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (pago_id)
        REFERENCES pagos(id)
);