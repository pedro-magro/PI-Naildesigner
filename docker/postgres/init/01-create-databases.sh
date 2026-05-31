#!/bin/sh
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE DATABASE ${AUTH_DB_NAME:-auth_db};
CREATE DATABASE ${SERVICO_DB_NAME:-servico_db};
CREATE DATABASE ${AGENDAMENTO_DB_NAME:-agendamento_db};
EOSQL
