CREATE DATABASE IF NOT EXISTS bancoxyz;
USE bancoxyz;

DROP TABLE IF EXISTS interes_entity;
DROP TABLE IF EXISTS cuenta_anual_entity;
DROP TABLE IF EXISTS transaccion_entity;

CREATE TABLE interes_entity (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    nombre VARCHAR(255),
    saldo_inicial DECIMAL(19, 2),
    edad INT,
    tipo_cuenta VARCHAR(100),
    saldo_final DECIMAL(19, 2),
    INDEX idx_interes_cuenta_id (cuenta_id)
);

CREATE TABLE cuenta_anual_entity (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT,
    fecha DATE,
    transaccion VARCHAR(100),
    monto DECIMAL(19, 2),
    descripcion VARCHAR(255)
);

CREATE TABLE transaccion_entity (
    id BIGINT NOT NULL PRIMARY KEY,
    fecha DATE,
    monto DECIMAL(19, 2),
    tipo VARCHAR(100),
    estado VARCHAR(50)
);

LOAD DATA INFILE '/var/lib/mysql-files/intereses.csv'
INTO TABLE interes_entity
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@cuenta_id, nombre, @saldo, @edad, tipo_cuenta)
SET cuenta_id = @cuenta_id,
    saldo_inicial = NULLIF(@saldo, ''),
    edad = NULLIF(@edad, ''),
    saldo_final = NULLIF(@saldo, '');

LOAD DATA INFILE '/var/lib/mysql-files/cuentas_anuales.csv'
INTO TABLE cuenta_anual_entity
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(cuenta_id, @fecha, transaccion, @monto, descripcion)
SET fecha = CASE
        WHEN @fecha REGEXP '^[0-9]{4}-' AND SUBSTRING(@fecha, 6, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%Y-%m-%d')
        WHEN @fecha REGEXP '^[0-9]{4}/' AND SUBSTRING(@fecha, 6, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%Y/%m/%d')
        WHEN @fecha REGEXP '^[0-9]{2}-' AND SUBSTRING(@fecha, 4, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%d-%m-%Y')
        WHEN @fecha REGEXP '^[0-9]{2}/' AND SUBSTRING(@fecha, 4, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%d/%m/%Y')
        ELSE NULL
    END,
    monto = NULLIF(@monto, '');

LOAD DATA INFILE '/var/lib/mysql-files/transacciones.csv'
INTO TABLE transaccion_entity
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(id, @fecha, @monto, tipo)
SET fecha = CASE
        WHEN @fecha REGEXP '^[0-9]{4}-' AND SUBSTRING(@fecha, 6, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%Y-%m-%d')
        WHEN @fecha REGEXP '^[0-9]{4}/' AND SUBSTRING(@fecha, 6, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%Y/%m/%d')
        WHEN @fecha REGEXP '^[0-9]{2}-' AND SUBSTRING(@fecha, 4, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%d-%m-%Y')
        WHEN @fecha REGEXP '^[0-9]{2}/' AND SUBSTRING(@fecha, 4, 2) BETWEEN '01' AND '12'
            THEN STR_TO_DATE(@fecha, '%d/%m/%Y')
        ELSE NULL
    END,
    monto = NULLIF(@monto, '');
