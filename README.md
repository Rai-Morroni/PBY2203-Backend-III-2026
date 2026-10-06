# Banco XYZ - Backend distribuido

Proyecto Java 21 y Spring Boot 3.4.1, organizado como un reactor Maven de microservicios. Incluye configuración centralizada, descubrimiento Eureka, emisión de JWT, tres BFF de canal, dos servicios de dominio, MySQL, carga CSV y un flujo de eventos Kafka para retiros.

> Proyecto de demostración académica. Las credenciales, contraseñas, claves TLS y configuración de seguridad son solo para desarrollo.

## Arquitectura

| Servicio | Puerto publicado | Funcionalidad |
|---|---:|---|
| `config-server` | 8888 HTTP | Publica configuración desde `config-server/src/main/resources/config-repo/`. |
| `eureka-server` | 8761 HTTP | Registro y descubrimiento de servicios. |
| `auth-server` | 9000 HTTP | Autentica usuarios de demostración, emite JWT RSA y publica metadatos OIDC/JWKS. |
| `mysql-db` | 3306 | Base MySQL `bancoxyz`. |
| `mysql-seed` | — | Contenedor de una sola ejecución que recrea las tablas de carga e importa los CSV. |
| `kafka` | 29092 desde el host; 9092 en Docker | Broker Kafka 3.7.0 con KRaft. |
| `ms-cuentas` | 8081 HTTP | Endpoint interno de consulta de saldo y acceso JPA. |
| `ms-transacciones` | 8082 HTTP | Historial, conteo de transacciones y consumidor de eventos de retiro. |
| `ms-bff-cajero` | 8443 HTTPS | Proxy de login, consulta de saldo y publicación asíncrona de retiros. |
| `ms-bff-mobile` | 8444 HTTPS | Endpoint de resumen móvil. Actualmente entrega datos fijos de demostración. |
| `ms-bff-web` | 8445 HTTPS | Dashboard web e historial formateado para el canal. |

Los servicios se conectan entre sí mediante los nombres DNS de Compose (`mysql-db`, `kafka`, `auth-server`, `ms-cuentas`, `ms-transacciones`, etc.). Las llamadas BFF→dominio no usan balanceo de carga Eureka.

## Tecnologías

- Java 21, Spring Boot 3.4.1 y Maven.
- Spring Cloud Config y Netflix Eureka.
- Spring Authorization Server y OAuth2 Resource Server.
- MySQL 8, Spring Data JPA e Hibernate.
- Apache Kafka 3.7.0.
- Resilience4j para tolerancia a fallos en consultas seleccionadas.
- Docker Compose para la ejecución integrada.

## Requisitos

- Docker Desktop con Docker Compose.
- JDK 21 y el Maven Wrapper (`mvnw.cmd`, incluido) para compilar las imágenes.

## Inicio principal: `iniciar_proyecto.bat`

En Windows, inicia Docker Desktop y ejecuta `iniciar_proyecto.bat` desde el Explorador o desde PowerShell en la raíz del repositorio:

```powershell
.\iniciar_proyecto.bat
```

El script comprueba que Docker esté disponible, empaqueta los módulos con `mvnw.cmd clean package -DskipTests`, construye las imágenes y levanta **todo el stack exclusivamente con Docker Compose**. No lanza instancias adicionales de Spring con Maven, por lo que evita duplicar procesos y puertos. Al final muestra el estado de los contenedores y las URLs principales.

La carga inicial de MySQL desde los CSV es intencional para esta demo: cada ejecución del script vuelve a crear y poblar las tres tablas base. Los datos generados durante una demostración no se conservan al reiniciar el proyecto.

`mysql-seed` espera a que MySQL esté saludable y termina con código `0` después de cargar los archivos. `ms-cuentas` y `ms-transacciones` esperan a que esa carga finalice.

Para consultar la carga:

```powershell
docker exec -it mysql-db mysql -u root -padmin -e "USE bancoxyz; SELECT COUNT(*) AS intereses FROM interes_entity; SELECT COUNT(*) AS cuentas_anuales FROM cuenta_anual_entity; SELECT COUNT(*) AS transacciones FROM transaccion_entity;"
```

Cada tabla de carga debería contener 1.000 filas. Ejecutar de nuevo el script o `mysql-seed` **elimina y recrea** `interes_entity`, `cuenta_anual_entity` y `transaccion_entity`, y vuelve a cargar los CSV; ese reinicio es el comportamiento esperado para las demostraciones.

Para repetir la importación manualmente:

```powershell
docker compose up -d mysql-seed
docker compose wait mysql-seed
```

Para revisar servicios y logs:

```powershell
docker compose ps -a
docker compose logs --tail 100 mysql-db mysql-seed
docker compose logs --tail 100 auth-server ms-cuentas ms-transacciones ms-bff-cajero ms-bff-web ms-bff-mobile
```

Para detener los servicios:

```powershell
docker compose down
```

No se declara un volumen con nombre para persistir MySQL. Independientemente de los detalles de almacenamiento del contenedor, al iniciar la demo se vuelven a cargar las tres tablas con los CSV para recuperar el estado base.

## Alternativas de ejecución

El `.bat` es el método recomendado para la ejecución. Usa una de estas alternativas solo si necesitas controlar Docker Compose o ejecutar las aplicaciones directamente desde Maven. 

### Alternativa A: Docker Compose manual

Primero empaqueta los JAR y luego construye e inicia el stack:

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose up -d --build
docker compose ps -a
```

Compose espera a MySQL y a la carga CSV antes de arrancar los dos servicios de dominio.

### Alternativa B: infraestructura en Docker y aplicaciones con Maven

Inicia solo MySQL, el seeder y Kafka en Docker:

```powershell
docker compose up -d mysql-seed kafka
```

Luego inicia cada servicio de aplicación en una terminal separada:

```powershell
.\mvnw.cmd -pl config-server spring-boot:run
.\mvnw.cmd -pl eureka-server spring-boot:run
.\mvnw.cmd -pl auth-server spring-boot:run
.\mvnw.cmd -pl ms-cuentas spring-boot:run
.\mvnw.cmd -pl ms-transacciones spring-boot:run
.\mvnw.cmd -pl ms-bff-cajero spring-boot:run
.\mvnw.cmd -pl ms-bff-web spring-boot:run
.\mvnw.cmd -pl ms-bff-mobile spring-boot:run
```

La configuración local usa MySQL en `localhost:3306` y Kafka en `localhost:29092`.

## Carga de datos y estado de persistencia

Los archivos de entrada están en `var/lib/mysql-files/`:

- `intereses.csv`
- `cuentas_anuales.csv`
- `transacciones.csv`

El script `sql/init.sql` carga esas columnas y sus destinos de staging dentro de MySQL:

| CSV | Tabla de carga | Filas esperadas |
|---|---|---:|
| `intereses.csv` | `interes_entity` | 1.000 |
| `cuentas_anuales.csv` | `cuenta_anual_entity` | 1.000 |
| `transacciones.csv` | `transaccion_entity` | 1.000 |

En el CSV de transacciones hay 55 fechas con mes `13`. Esas filas se conservan, pero su columna `fecha` se carga como `NULL`.

**Inconsistencia pendiente en intereses:** `ms-cuentas` tiene su entidad JPA asociada a `saldos_intereses`, mientras que el seeder importa el CSV en `interes_entity`. Hibernate puede crear `saldos_intereses` como otra tabla; el endpoint de saldo consulta esa tabla y no los datos importados en `interes_entity`. Por lo tanto, que `interes_entity` tenga 1.000 filas no implica que el saldo consumido por Cajero tenga datos.

`ms-transacciones` sí mapea `cuenta_anual_entity` y `transaccion_entity`, las tablas de carga correspondientes.

## Configuración y seguridad

- Config Server lee archivos con nombres correspondientes a `spring.application.name`, por ejemplo `auth-server.yml` y `ms-bff-web.yml`.
- Los BFF exponen HTTPS con certificados de desarrollo `keystore.p12`. `curl.exe` necesita `-k` para aceptarlos localmente.
- Las URLs entre contenedores y el issuer JWT se configuran con variables en `docker-compose.yml`.
- La clave RSA del Auth Server se genera en memoria al iniciar. Al reiniciarlo, los JWT anteriores dejan de validar.
- Usuarios y clientes OAuth2 se mantienen en memoria; no son almacenamiento persistente.
- MySQL usa `root` / `admin` para desarrollo. No son credenciales apropiadas para producción.
- Las APIs `/api/internal/**` de los servicios de dominio no tienen autenticación propia y deben considerarse internas.

### Usuarios de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `cliente_cajero` | `cajero123` | `ROLE_CAJERO` |
| `cliente_web` | `web123` | `ROLE_WEB` |
| `cliente_movil` | `movil123` | `ROLE_MOBILE` |

El servidor registra el cliente público `bancoxyz-client` con Authorization Code + PKCE y OpenID Connect. Para las pruebas de los BFF, el proyecto además expone el endpoint de compatibilidad `POST /api/auth/login`, que autentica el usuario y emite un JWT firmado con RSA. El BFF Cajero expone un proxy del mismo login.

## APIs disponibles

| Servicio | Método y ruta | Autorización / resultado |
|---|---|---|
| Auth Server | `POST http://localhost:9000/api/auth/login` | Público. Devuelve `access_token`, `token`, `token_type` y `expires_in`. |
| Auth Server | `GET /.well-known/openid-configuration` | Metadatos OIDC. |
| Auth Server | `GET /oauth2/jwks` | Claves públicas para verificar JWT. |
| BFF Cajero | `POST https://localhost:8443/api/auth/login` | Público; proxy y respuesta compatible `{ "token": "..." }`. |
| BFF Cajero | `GET /api/cajero/saldo` | Requiere `ROLE_CAJERO`; consulta `ms-cuentas`. |
| BFF Cajero | `POST /api/cajero/retiro` | Requiere `ROLE_CAJERO`; valida el cuerpo y publica un evento Kafka. |
| BFF Web | `GET https://localhost:8445/api/web/dashboard` | Requiere `ROLE_WEB`; devuelve dashboard e historial. |
| BFF Mobile | `GET https://localhost:8444/api/mobile/resumen` | Requiere `ROLE_MOBILE`; devuelve un resumen de demostración. |
| `ms-cuentas` | `GET http://localhost:8081/api/internal/cuentas/saldo` | Interno; consulta `saldos_intereses`. |
| `ms-transacciones` | `GET http://localhost:8082/api/internal/transacciones/historial` | Interno; devuelve cuentas anuales. |
| `ms-transacciones` | `GET http://localhost:8082/api/internal/transacciones/transacciones/count` | Interno; cuenta las filas de `transaccion_entity`. |

El retiro requiere un cuerpo JSON; `cuentaId` no puede estar vacío y el monto mínimo es 1.000:

```json
{
  "cuentaId": "123",
  "monto": 50000
}
```

## Ejemplos de consumo desde PowerShell

Solicita tokens para los tres canales:

```powershell
$body = @{ username = 'cliente_cajero'; password = 'cajero123' } | ConvertTo-Json
$cajeroToken = (Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/api/auth/login' -ContentType 'application/json' -Body $body).access_token

$body = @{ username = 'cliente_web'; password = 'web123' } | ConvertTo-Json
$webToken = (Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/api/auth/login' -ContentType 'application/json' -Body $body).access_token

$body = @{ username = 'cliente_movil'; password = 'movil123' } | ConvertTo-Json
$mobileToken = (Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/api/auth/login' -ContentType 'application/json' -Body $body).access_token
```

Consume las APIs protegidas:

```powershell
curl.exe -k -i -H "Authorization: Bearer $cajeroToken" https://localhost:8443/api/cajero/saldo
curl.exe -k -i -H "Authorization: Bearer $webToken" https://localhost:8445/api/web/dashboard
curl.exe -k -i -H "Authorization: Bearer $mobileToken" https://localhost:8444/api/mobile/resumen
```

Publica un retiro:

```powershell
curl.exe -k -i -X POST https://localhost:8443/api/cajero/retiro `
  -H "Authorization: Bearer $cajeroToken" `
  -H "Content-Type: application/json" `
  --data-raw '{"cuentaId":"123","monto":50000}'
```

Comprueba autorización por roles:

```powershell
curl.exe -k -i https://localhost:8443/api/cajero/saldo
curl.exe -k -i -H "Authorization: Bearer $mobileToken" https://localhost:8443/api/cajero/saldo
```

Se espera `401` sin token y `403` al presentar un token válido que no tiene el rol requerido.

## Retiro asíncrono: alcance actual

`ms-bff-cajero` publica el evento `cajero-retiros-topic`; `ms-transacciones` lo consume y lo escribe en sus logs. El consumidor actual **no persiste el retiro en MySQL, no descuenta saldos ni emite una confirmación de resultado**. Este flujo demuestra mensajería asíncrona, pero todavía no implementa una Saga financiera completa.

El límite de consultas y retiros del BFF Cajero se mantiene en memoria del proceso; no se comparte entre réplicas y se reinicia al reiniciar el servicio.

## Validaciones

Los módulos actuales han sido comprobados manualmente junto con sus endpoints.
