@echo off
setlocal ENABLEDELAYEDEXPANSION

REM ============================
REM 1) Rutas base
REM ============================

REM Directorio raíz del proyecto = carpeta donde está este .bat
set "ROOT_DIR=%~dp0"
REM quitar barra final si la hay
if "%ROOT_DIR:~-1%"=="\" set "ROOT_DIR=%ROOT_DIR:~0,-1%"

set "COMPOSE=%ROOT_DIR%\docker-compose.yml"

echo Using docker-compose: %COMPOSE%
echo.

REM ============================
REM 2) Bajar stack previo
REM ============================

echo Stopping and removing previous stack...
docker-compose -f "%COMPOSE%" down -v
echo.

REM ============================
REM 3) Levantar infraestructura base
REM ============================

echo Starting infrastructure: postgres, mongo, zookeeper, kafka...
docker-compose -f "%COMPOSE%" up -d --build postgres mongo zookeeper kafka
echo.

REM IMPORTANTE:
REM Usa el nombre REAL del contenedor de Postgres según docker-compose.
REM Por los logs, es "orders_postgres" (con guion bajo).
set "PG_CONTAINER=orders_postgres"

echo Waiting for Postgres to be ready...

:wait_pg
docker exec %PG_CONTAINER% pg_isready -U orders_user -d orders_db >NUL 2>&1
IF ERRORLEVEL 1 (
    echo   Postgres not ready yet, retrying in 3 seconds...
    timeout /t 3 /nobreak >NUL
    goto wait_pg
)

echo Postgres is ready.
echo.

REM ============================
REM 4) Ejecutar SQL inicial
REM ============================

set "SQL_DIR=%ROOT_DIR%\db\postgres"

if exist "%SQL_DIR%" (
    echo Running initial SQL scripts from: %SQL_DIR%

    dir /b "%SQL_DIR%\*.sql" >NUL 2>&1
    if ERRORLEVEL 1 (
        echo   No .sql files found in %SQL_DIR%
    ) else (
        for %%F in ("%SQL_DIR%\*.sql") do (
            echo   Executing %%F ...
            type "%%F" | docker exec -i %PG_CONTAINER% psql -U orders_user -d orders_db -v ON_ERROR_STOP=1 -f -
        )
        echo   SQL initialization finished.
    )
) else (
    echo WARNING: SQL directory not found: %SQL_DIR%
    echo          Create that folder and put your .sql files there if needed.
)

echo.

REM ============================
REM 5) Levantar servicios de la app
REM ============================

echo Starting application services: order-service, payment-service, notification-service...
docker-compose -f "%COMPOSE%" up -d --build order-service payment-service notification-service

echo.
echo ==================================================
echo Installation completed.
echo   - Order Service:        http://localhost:8080
echo   - Payment Service:      http://localhost:8081
echo   - Notification Service: http://localhost:8082
echo ==================================================
echo.

endlocal
