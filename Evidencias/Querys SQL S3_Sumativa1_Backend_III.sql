USE bancoxyz;

-- ! Limpieza de Datos (Restablecer Entorno para Pruebas)
TRUNCATE TABLE transacciones;
TRUNCATE TABLE saldos_intereses;
TRUNCATE TABLE estados_cuenta_anual;

-- Job 1: Transacciones Diarias
-- I. Ver todas las transacciones procesadas 
SELECT * FROM transacciones;

-- II. Reporte de anomalías detectadas
SELECT id, fecha, monto, tipo, estado 
FROM transacciones 
WHERE estado IN ('ANOMALIA', 'ANOMALIA_NULO');

-- III. Resumen de estado de transacciones
SELECT estado, COUNT(*) AS total_transacciones, SUM(monto) AS monto_total
FROM transacciones
GROUP BY estado;


-- Job 2: Cálculo de Intereses Mensuales
-- I. Ver los saldos finales recalculados
SELECT * FROM saldos_intereses;

-- II. Reporte de calculo de intereses mensuales por ID de cuenta
SELECT cuenta_id, tipo_cuenta, saldo_inicial, saldo_final
FROM saldos_intereses
WHERE tipo_cuenta IN ('AHORRO', 'PRESTAMO');


-- Job 3: Estados de Cuenta Anuales
-- 1. Ver los estados de cuenta procesados
SELECT * FROM estados_cuenta_anual;

-- 2. Reporte de auditoria de cuentas
SELECT cuenta_id, fecha, transaccion, monto, descripcion 
FROM estados_cuenta_anual 
ORDER BY cuenta_id ASC;



-- Ver el Estado General de los Jobs Ejecutados
SELECT 
    e.JOB_EXECUTION_ID,
    i.JOB_NAME,
    e.STATUS,
    e.EXIT_CODE,
    e.START_TIME,
    e.END_TIME,
    TIMEDIFF(e.END_TIME, e.START_TIME) AS duracion
FROM BATCH_JOB_EXECUTION e
JOIN BATCH_JOB_INSTANCE i ON e.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID
ORDER BY e.START_TIME DESC;

-- Ver el Rendimiento de los Steps (Hilos, Lecturas, Escrituras y Omisiones)
SELECT 
    STEP_NAME,
    STATUS,
    READ_COUNT AS registros_leidos,
    WRITE_COUNT AS registros_guardados,
    READ_SKIP_COUNT AS registros_omitidos_lectura,
    PROCESS_SKIP_COUNT AS registros_omitidos_proceso,
    COMMIT_COUNT AS chunks_procesados
FROM BATCH_STEP_EXECUTION
ORDER BY STEP_EXECUTION_ID DESC;



