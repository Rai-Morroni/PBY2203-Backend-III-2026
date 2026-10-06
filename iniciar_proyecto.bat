@echo off
setlocal
pushd "%~dp0"

echo =======================================================
echo Banco XYZ - inicio de la demo
echo =======================================================
echo Este script compila y levanta todos los servicios con Docker Compose.
echo MySQL volvera a cargar los datos base desde los CSV en cada ejecucion.
echo.

where.exe docker >NUL 2>&1
if errorlevel 1 (
    echo ERROR: Docker CLI no esta instalado o no esta disponible en PATH.
    goto :error
)

docker info >NUL 2>&1
if errorlevel 1 (
    echo ERROR: Docker Desktop no esta iniciado o no esta accesible.
    goto :error
)

echo [1/3] Empaquetando los microservicios con Maven...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Fallo la compilacion Maven. Revisa la salida anterior.
    goto :error
)

echo.
echo [2/3] Construyendo e iniciando el stack completo...
docker compose up -d --build
if errorlevel 1 (
    echo ERROR: Docker Compose no pudo iniciar el stack.
    docker compose ps -a
    goto :error
)

echo.
echo Verificando que todos los microservicios sigan ejecutandose...
for %%S in (config-server eureka-server auth-server ms-cuentas ms-transacciones ms-bff-cajero ms-bff-web ms-bff-mobile) do (
    docker compose ps --status running -q %%S | findstr . >NUL
    if errorlevel 1 (
        echo ERROR: El servicio %%S no esta ejecutandose. Ultimos logs:
        docker compose logs --tail 60 %%S
        goto :error
    )
)

echo.
echo [3/3] Estado de los contenedores:
docker compose ps -a
if errorlevel 1 (
    echo ERROR: No se pudo consultar el estado de Docker Compose.
    goto :error
)

echo.
echo =======================================================
echo Inicio solicitado correctamente.
echo Config Server:    http://localhost:8888
echo Eureka:           http://localhost:8761
echo Auth Server:      http://localhost:9000
echo BFF Cajero:       https://localhost:8443
echo BFF Mobile:       https://localhost:8444
echo BFF Web:          https://localhost:8445
echo.
echo Ver logs: docker compose logs -f
echo Detener:  docker compose down
echo =======================================================
popd
pause
exit /b 0

:error
echo.
echo El inicio no se completo. Corrige el problema indicado y vuelve a ejecutar este archivo.
popd
pause
exit /b 1
