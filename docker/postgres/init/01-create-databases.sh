#!/bin/bash
set -euo pipefail

# The official PostgreSQL image runs this file only for an empty data volume.
for database_name in "$CATALOGUE_DB_NAME" "$ORDER_DB_NAME" "$CATALOGUE_TEST_DB_NAME" "$ORDER_TEST_DB_NAME"; do
    if [ -z "$database_name" ]; then
        echo "Database names must not be empty." >&2
        exit 1
    fi

    psql --username "$POSTGRES_USER" --dbname postgres --set ON_ERROR_STOP=1 \
        --set database_name="$database_name" --set database_owner="$POSTGRES_USER" <<'SQL'
SELECT format('CREATE DATABASE %I OWNER %I', :'database_name', :'database_owner')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'database_name')
\gexec
SQL
done
