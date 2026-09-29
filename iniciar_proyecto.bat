@echo off
echo =======================================================
echo Iniciando Ecosistema de Microservicios - Banco XYZ
echo =======================================================

echo 1. Iniciando infraestructura en Docker (MySQL y Kafka)...
docker compose up -d
timeout /t 10 /nobreak > NUL

echo 2. Levantando Config Server (Puerto 8888)...
start "Config Server" cmd /k "cd config-server && ..\mvnw spring-boot:run"
:: Esperamos 15 segundos para asegurar que el servidor de configuracion este listo
timeout /t 15 /nobreak > NUL

echo 3. Levantando Service Discovery Eureka (Puerto 8761)...
start "Eureka Server" cmd /k "cd eureka-server && ..\mvnw spring-boot:run"
:: Esperamos 15 segundos para que Eureka este listo para recibir registros
timeout /t 15 /nobreak > NUL

echo 4. Levantando Microservicio Core (BD y Consumidor Kafka)...
start "MS-Core" cmd /k "cd ms-core && ..\mvnw spring-boot:run"
:: Esperamos 15 segundos para que Core se conecte a BD y se registre en Eureka
timeout /t 15 /nobreak > NUL

echo 5. Levantando Microservicios BFF...
start "BFF Cajero" cmd /k "cd ms-bff-cajero && ..\mvnw spring-boot:run"
start "BFF Mobile" cmd /k "cd ms-bff-mobile && ..\mvnw spring-boot:run"
start "BFF Web" cmd /k "cd ms-bff-web && ..\mvnw spring-boot:run"

echo =======================================================
echo Todos los comandos de inicio han sido lanzados.
echo Por favor, revisa las nuevas ventanas de consola para 
echo confirmar que cada servicio inicio correctamente.
echo =======================================================
pause