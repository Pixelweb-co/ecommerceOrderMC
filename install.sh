#!/usr/bin/env bash
set -euo pipefail

# ============================
# 1) Rutas base
# ============================

# Directorio raíz del proyecto = carpeta donde está este script
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

COMPOSE="$ROOT_DIR/docker-compose.yml"

echo "Using docker-compose: $COMPOSE"
echo

# ============================
# 2) Bajar stack previo
# ============================

echo "Stopping and removing previous stack..."
docker-compose -f "$COMPOSE" down -v || true
echo

# ============================
# 3) Levantar infraestructura base
# ============================

echo "Starting infrastructure: postgres, mongo, zookeeper, kafka..."
docker-compose -f "$COMPOSE" up -d --build postgres mongo zookeeper kafka
echo

# IMPORTANTE:
# Usa el nombre REAL del contenedor de Postgres según docker-compose.
# En tu caso: "orders_postgres" (con guion bajo).
PG_CONTAINER="orders_postgres"

echo "Waiting for Postgres to be ready..."

while true; do
  if docker exec "$PG_CONTAINER" pg_isready -U orders_user -d orders_db > /dev/null 2>&1; then
    break
  fi
  echo "  Postgres not ready yet, retrying in 3 seconds..."
  sleep 3
done

echo "Postgres is ready."
echo

# ============================
# 4) Ejecutar SQL inicial
# ============================

SQL_DIR="$ROOT_DIR/db/postgres"

if [ -d "$SQL_DIR" ]; then
  echo "Running initial SQL scripts from: $SQL_DIR"

  shopt -s nullglob
  sql_files=("$SQL_DIR"/*.sql)
  if [ ${#sql_files[@]} -eq 0 ]; then
    echo "  No .sql files found in $SQL_DIR"
  else
    for sql_file in "${sql_files[@]}"; do
      echo "  Executing $sql_file ..."
      docker exec -i "$PG_CONTAINER" psql \
        -U orders_user \
        -d orders_db \
        -v ON_ERROR_STOP=1 \
        -f - < "$sql_file"
    done
    echo "  SQL initialization finished."
  fi
  shopt -u nullglob
else
  echo "WARNING: SQL directory not found: $SQL_DIR"
  echo "         Create that folder and put your .sql files there if needed."
fi

echo

# ============================
# 5) Levantar servicios de la app
# ============================

echo "Starting application services: order-service, payment-service, notification-service..."
docker-compose -f "$COMPOSE" up -d --build order-service payment-service notification-service

echo
echo "=================================================="
echo "Installation completed."
echo "  - Order Service:        http://localhost:8080"
echo "  - Payment Service:      http://localhost:8081"
echo "  - Notification Service: http://localhost:8082"
echo "=================================================="
echo
