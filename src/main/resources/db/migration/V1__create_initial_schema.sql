-- ==========================================
-- V1: ESQUEMA CORE DEL SISTEMA
-- ==========================================

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(55) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(55) NOT NULL,
    apellido VARCHAR(55) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    fecha_registro DATETIME NOT NULL,
    fecha_actualizacion DATETIME NOT NULL,

    CONSTRAINT fk_rol_usuario
        FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE eventos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_evento DATETIME NOT NULL,
    ubicacion VARCHAR(255) NOT NULL,
    capacidad_total INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    edad_minima INT,
    fecha_fin DATETIME,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE tipos_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock_total INT NOT NULL,
    stock_disponible INT NOT NULL,
    fecha_inicio_venta DATETIME,
    fecha_fin_venta DATETIME,

    CONSTRAINT fk_tipo_entrada_evento
        FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE CASCADE
);