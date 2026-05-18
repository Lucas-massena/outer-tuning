#!/bin/bash
set -e

PG_HOST="${PG_HOST:-postgres}"
PG_PORT="${PG_PORT:-5432}"
PG_USER="${PG_USER:-postgres}"
export PGPASSWORD="${PGPASSWORD:-postgres}"

echo "=== Waiting for PostgreSQL at ${PG_HOST}:${PG_PORT} ==="
until psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d postgres -c '\q' 2>/dev/null; do
    echo "  waiting..."
    sleep 3
done
echo "PostgreSQL is ready"

cd /home/HammerDB-5.0

DB_EXISTS=$(psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d postgres \
    -tAc "SELECT 1 FROM pg_database WHERE datname='tpcc'" 2>/dev/null || echo "")

if [ "$DB_EXISTS" = "1" ]; then
    echo "=== TPC-C database already exists, skipping build ==="
else
    echo "=== Building TPC-C schema (10 warehouses) - this may take several minutes ==="
    ./hammerdbcli auto /scripts/tpcc_build.tcl
    echo "=== Schema build complete ==="

    echo "=== Enabling HypoPG on tpcc database ==="
    psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d tpcc \
        -c "CREATE EXTENSION IF NOT EXISTS hypopg;" || true
fi

echo "=== Starting TPC-C workload (cycles of 1 hour) ==="
while true; do
    ./hammerdbcli auto /scripts/tpcc_run.tcl || true
    echo "Workload cycle complete. Restarting in 5s..."
    sleep 5
done
