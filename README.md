# Bank Batch Legacy

## Objetivo
Desarrollar una aplicación de procesamiento batch cloud-native para modernizar el sistema legacy del Banco XYZ. El proyecto lee información financiera desde archivos CSV, aplica reglas de validación y transformaciones de negocio mediante `ItemProcessor`, y persiste los resultados en una base de datos MySQL. Se implementan políticas personalizadas de tolerancia a fallos y estrategias de escalamiento multihilo para procesar datos inconsistentes de forma rápida y segura.

## Tecnologias

- Java 21
- Spring Boot 3.4.1
- Spring Batch
- Spring Data JPA / Hibernate
- MySQL 8 o compatible
- Maven Wrapper
- Lombok
- Spring Boot Actuator

## Requisitos

- JDK 21 configurado en `JAVA_HOME`.
- MySQL en ejecucion.
- Maven no es necesario: el proyecto incluye `mvnw` y `mvnw.cmd`.

## Base de datos

La aplicacion espera una base de datos llamada `bancoxyz`:

```sql
CREATE DATABASE bancoxyz;
```

La configuracion predeterminada se encuentra en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bancoxyz
spring.datasource.username=root
spring.datasource.password=admin
```

Antes de ejecutar el proyecto, se debe cambiar las credenciales si la instalacion de MySQL usa otros valores. 

Hibernate crea o actualiza las tablas de negocio automaticamente mediante `spring.jpa.hibernate.ddl-auto=update`. Spring Batch inicializa sus tablas de metadatos con `spring.batch.jdbc.initialize-schema=always`.

## Ejecucion

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

El job seleccionado por defecto es `reporteDiarioJob`, debido a esta propiedad:

```properties
# Job 1: 
spring.batch.job.name=reporteDiarioJob
```

Tambien se puede ejecutar un job especifico sobrescribiendo la propiedad en la linea de comandos:

```powershell
# Job 2:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.batch.job.name=calculoInteresesJob"

# Job 3:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.batch.job.name=estadoCuentaAnualJob"
```


## Estrategia de Escalamiento y Resultados Obtenidos
Para optimizar el rendimiento, se compararon distintas configuraciones de procesamiento:

1. **Procesamiento Lineal (Single-Thread):**

    - **Configuración**: Ejecución estándar secuencial sin TaskExecutor.

    - **Resultados**: Procesamiento estable y seguro, sin bloqueos a nivel de base de datos. Sin embargo, el tiempo total de ejecución fue elevado debido a la espera síncrona de I/O en cada chunk.

2. **Procesamiento Concurrente (Multi-Thread) - Configuración Final:**

- **Configuración**: Uso de ThreadPoolTaskExecutor con corePoolSize=3, maxPoolSize=5 y procesamiento en chunk(5).

- **Resultados**: Reducción drástica del tiempo de procesamiento. Durante la ejecución del cálculo de intereses, la concurrencia generó contención de datos (Deadlocks / Duplicate Entries) por registros legacy duplicados. Esto validó la eficacia de la arquitectura, ya que la política de tolerancia a fallos de Spring Batch manejó los rollbacks automáticamente, aislando los errores y permitiendo que los Jobs finalizaran con estado **COMPLETED**.


## Arquitectura Backend for Frontend (BFF) y Seguridad

Para optimizar la comunicación entre el sistema y los diferentes clientes, se implementó el patrón **Backend for Frontend (BFF)** utilizando la estrategia de **Diseño de Endpoints Personalizados**.

*   **BFF Web (`/api/web/**`):** Optimizado para navegadores de escritorio, entregando payloads complejos y detallados para interfaces robustas.
*   **BFF Móvil (`/api/mobile/**`):** Diseñado con respuestas ligeras y datos esenciales, minimizando el consumo de ancho de banda.
*   **BFF Cajero Automático (`/api/cajero/**`):** Interfaz enfocada en la seguridad y eficiencia para operaciones críticas (ej. retiros y consultas de saldo).

## Justificación de la Estrategia Arquitectónica (Enfoque Transicional)

Dado que el núcleo del proyecto consiste en un procesamiento Spring Batch construido sobre una arquitectura monolítica, se optó por implementar los BFF como controladores lógicos especializados dentro del mismo contexto de aplicación. En lugar de realizar peticiones de red externas hacia microservicios independientes, estos controladores se comunican directamente con la capa de datos mediante repositorios Spring Data JPA.

Esta decisión de enfoque permite:

* **Cumplir el objetivo principal del patrón BFF**: Transformar, filtrar y adaptar la estructura de los datos según las restricciones y capacidades de cada canal cliente (payload ligero vs. pesado).

* **Evitar sobrecarga**: Elimina la latencia de red y la complejidad de orquestación en esta etapa de modernización transicional del sistema legacy.

* **Facilitar la escalabilidad futura**: Al mantener una estricta modularidad a nivel de paquetes (controller aislado de repository), se prepara el terreno para una eventual extracción de la capa de acceso a datos hacia verdaderos microservicios distribuidos.

### Seguridad Implementada
El sistema está securizado bajo los siguientes estándares:
*   **Protocolo HTTPS:** Cifrado de extremo a extremo configurado a través de un certificado SSL (Keystore local) activo en el puerto `8443`.
*   **Autenticación y Autorización (JWT):** Se migró hacia un modelo Stateless basado en JSON Web Tokens (Bearer Token). Se definieron roles estrictos (`WEB`, `MOBILE`, `CAJERO`).
    * **Existe segregación de acceso cruzada por canal**: un token generado para un cliente móvil es rechazado con un error `403 Forbidden` si intenta consumir recursos del BFF Web o Cajero, garantizando la confidencialidad de la información crítica.


## Jobs disponibles

| Job | Step | Archivo de entrada | Tabla de salida | Tolerancia |
|---|---|---|---|---:|
| `estadoCuentaAnualJob` | `estadoCuentaAnualStep` | `cuentas_anuales.csv` | `estados_cuenta_anual` | 100 errores |
| `calculoInteresesJob` | `calculoInteresesStep` | `intereses.csv` | `saldos_intereses` | 50 errores |
| `reporteDiarioJob` | `transaccionesStep` | `transacciones.csv` | `transacciones` | 100 errores |

Todos los jobs procesan en chunks de 5 registros y utilizan un `TaskExecutor` para procesamiento concurrente. Cada job tiene su propio executor, excepto el job de transacciones, que usa el executor general definido en su configuracion.

## Reglas por Proceso:
- **Transacciones**: Parsea dinámicamente formatos de fecha múltiples (yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy, yyyy/MM/dd). Intercepta montos nulos y marca anomalías para valores negativos o > $1.000.000.

- **Intereses**: Aplica +3% a cuentas de AHORRO y +5% de cargo a PRÉSTAMOS. Tolerancia estricta a IDs nulos.

- **Cuentas Anuales**: Genera llaves primarias autoincrementales para evitar colisiones multihilo.

## Archivos CSV

Los archivos se encuentran en `src/main/resources` y se leen desde el classpath. Todos incluyen una fila de encabezados, que se omite durante la lectura.

### `cuentas_anuales.csv`

Columnas: `cuenta_id`, `fecha`, `transaccion`, `monto`, `descripcion`.

- Soporta fechas `yyyy-MM-dd`, `dd-MM-yyyy`, `dd/MM/yyyy` y `yyyy/MM/dd`.
- Un monto vacio se transforma en `0`.
- Un `cuenta_id` nulo o una fecha invalida provocan el descarte del registro.

### `intereses.csv`

Columnas: `cuenta_id`, `nombre`, `saldo`, `edad`, `tipo`.

- Un `saldo` vacio se transforma en `0`.
- `AHORRO` aplica un interes del 3 por ciento.
- `PRESTAMO` aplica un interes del 5 por ciento.
- Otros tipos no modifican el saldo.
- El saldo final se redondea a 2 decimales.
- Un `cuenta_id` nulo provoca el descarte del registro.

### `transacciones.csv`

Columnas: `id`, `fecha`, `monto`, `tipo`.

- Soporta fechas `yyyy-MM-dd`, `dd-MM-yyyy`, `dd/MM/yyyy` y `yyyy/MM/dd`.
- Un monto vacio se convierte en `0` y queda con estado `ANOMALIA_NULO`.
- Montos negativos o mayores que `1000000` quedan con estado `ANOMALIA`.
- Los demas montos quedan con estado `NORMAL`.
- Una fecha invalida provoca el descarte del registro.

Los pasos estan configurados como tolerantes a fallos mediante `skip(Exception.class)`. Cuando se supera el `skipLimit` del job, el procesamiento falla.

## Estructura del proyecto

```text
src/
├── main/java/com/bancoxyz/batch_legacy/
│   ├── config/       Configuracion de jobs, steps, readers y writers
|   ├── controller/   Controladores y Maps para endpoints BFF
│   ├── model/        DTOs de entrada y entidades JPA
│   ├── processor/    Transformaciones y reglas de negocio
│   └── repository/   Repositorios Spring Data JPA
└── main/resources/
    ├── application.properties
    ├── cuentas_anuales.csv
    ├── intereses.csv
    └── transacciones.csv
```

## Verificacion de resultados

Despues de ejecutar un job, se pueden consultar las tablas creadas en MySQL:

```sql
USE bancoxyz;

SELECT * FROM estados_cuenta_anual;
SELECT * FROM saldos_intereses;
SELECT * FROM transacciones;
```

Spring Batch registra la ejecucion de jobs y steps en tablas como `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION` y `BATCH_STEP_EXECUTION`.

## Consultas SQL para consultar cada reporte:

``` sql
-- Job 1: Transacciones Diarias
-- Reporte de anomalías detectadas
SELECT id, fecha, monto, tipo, estado 
FROM transacciones 
WHERE estado IN ('ANOMALIA', 'ANOMALIA_NULO');
```

``` sql
-- Job 2: Cálculo de Intereses Mensuales
-- Reporte de calculo de intereses mensuales por ID de cuenta
SELECT cuenta_id, tipo_cuenta, saldo_inicial, saldo_final
FROM saldos_intereses
WHERE tipo_cuenta IN ('AHORRO', 'PRESTAMO');
```

``` sql
-- Job 3: Estados de Cuenta Anuales
-- Reporte de auditoria de cuentas
SELECT cuenta_id, fecha, transaccion, monto, descripcion 
FROM estados_cuenta_anual 
ORDER BY cuenta_id ASC;
```

## Consideraciones

- Los archivos CSV incluidos contienen fechas, montos y tipos invalidos para forzar la tolerancia a fallos.
- Los jobs escriben en tablas persistentes; **una ejecucion repetida puede generar conflictos o duplicados** segun la clave primaria de cada entidad y el estado registrado por Spring Batch.
- Para forzar una re-ejecución, se recomienda limpiar las tablas de metadatos de Spring Batch.
- Las credenciales del archivo de propiedades son valores de desarrollo.
- No se deben modificar los encabezados CSV sin actualizar los nombres definidos en los `FlatFileItemReader` correspondientes.
