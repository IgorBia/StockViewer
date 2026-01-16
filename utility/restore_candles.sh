#!/usr/bin/env bash
set -euo pipefail

# utility/restore_candles.sh
# Usage:
#   ./utility/restore_candles.sh [-f backup-file.sql] [--latest] [--container NAME] [--compose-cmd "docker compose"] [--yes] [--restart]
#
# - If -f not provided and --latest not provided, lists backups and exits.
# - --latest picks newest file in ./backups
# - --restart: after restore runs 'docker compose up -d' to bring services back
# - --yes: skip interactive confirmation

BACKUP_DIR="./backups"
FILE=""
CONTAINER="stockviewer-postgres"
COMPOSE_CMD=""
AUTO_YES=false
DO_RESTART=false

print_usage() {
  cat <<EOF
Usage: $0 [-f <backup-file.sql>] [--latest] [--container NAME] [--compose-cmd "docker compose"] [--yes] [--restart]

Examples:
  # restore specific file
  $0 -f ./backups/stock_candles_backup_20251212-140941.sql

  # restore newest file
  $0 --latest

  # auto accept and restart compose afterwards
  $0 --latest --yes --restart
EOF
  exit 1
}

# parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    -f) FILE="$2"; shift 2 ;;
    --latest) FILE="__LATEST__"; shift ;;
    --container) CONTAINER="$2"; shift 2 ;;
    --compose-cmd) COMPOSE_CMD="$2"; shift 2 ;;
    --yes) AUTO_YES=true; shift ;;
    --restart) DO_RESTART=true; shift ;;
    -h|--help) print_usage ;;
    *) echo "Unknown arg: $1"; print_usage ;;
  esac
done

# detect compose
detect_compose() {
  if [[ -n "$COMPOSE_CMD" ]]; then
    echo "$COMPOSE_CMD"
    return
  fi
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    echo "docker compose"
    return
  fi
  if command -v docker-compose >/dev/null 2>&1; then
    echo "docker-compose"
    return
  fi
  echo ""
}

COMPOSE="$(detect_compose)"
if [[ -z "$COMPOSE" ]]; then
  echo "ERROR: no docker compose command found (tried 'docker compose' and 'docker-compose')"
  exit 2
fi

# pick file
if [[ -z "$FILE" ]]; then
  echo "Backups in ${BACKUP_DIR}:"
  ls -1 "${BACKUP_DIR}" || true
  echo
  echo "No file specified. Pass -f <file> or --latest."
  exit 0
fi

if [[ "$FILE" == "__LATEST__" ]]; then
  FILE="$(ls -1t "${BACKUP_DIR}"/*.sql 2>/dev/null | head -n1 || true)"
  if [[ -z "$FILE" ]]; then
    echo "No .sql files found in ${BACKUP_DIR}"
    exit 3
  fi
fi

if [[ ! -f "$FILE" ]]; then
  echo "Backup file not found: $FILE"
  exit 4
fi

echo "Selected backup: $FILE"

if [[ "$AUTO_YES" != "true" ]]; then
  read -p "Proceed to restore this file into DB in container '$CONTAINER'? (y/N) " ans
  case "$ans" in
    [yY][eE][sS]|[yY]) ;;
    *) echo "Aborted."; exit 0;;
  esac
fi

echo "Starting restore process..."

# Ensure container exists or bring up postgres only
if ! docker inspect "$CONTAINER" >/dev/null 2>&1; then
  echo "Container '$CONTAINER' not found. Starting compose (postgres)..."
  $COMPOSE down
  $COMPOSE up -d postgres
fi

# wait until container is running
echo "Waiting for container '$CONTAINER' to be running..."
for i in $(seq 1 30); do
  if docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null | grep -q true; then
    break
  fi
  sleep 1
  if [[ $i -eq 30 ]]; then
    echo "Container did not become running in time"; exit 5
  fi
done

# wait for pg_isready inside container
echo "Waiting for Postgres readiness..."
for i in $(seq 1 60); do
  if docker exec "$CONTAINER" sh -c "pg_isready -U ${DB_USER:-user} -d ${DB_NAME:-stockviewer} >/dev/null 2>&1"; then
    echo "Postgres ready"; break
  fi
  sleep 1
  if [[ $i -eq 60 ]]; then
    echo "Postgres did not become ready in time"; exit 6
  fi
done

# copy and restore
TMP_DEST="/tmp/restore_$(basename "$FILE")"
echo "Copying $FILE -> $CONTAINER:$TMP_DEST"
docker cp "$FILE" "$CONTAINER":"$TMP_DEST"

echo "Cleaning existing candles & indicators data..."
docker exec -i "$CONTAINER" psql -U "${DB_USER:-user}" -d "${DB_NAME:-stockviewer}" -c "DROP TABLE IF EXISTS stock_data.indicator CASCADE; DROP TABLE IF EXISTS stock_data.candle CASCADE;"

echo "Running restore (psql -f $TMP_DEST)..."
docker exec -i "$CONTAINER" sh -c "psql -v ON_ERROR_STOP=1 -U ${DB_USER:-user} -d ${DB_NAME:-stockviewer} -f '$TMP_DEST'"

EXIT_CODE=$?

# cleanup
echo "Removing $TMP_DEST from container"
docker exec -i "$CONTAINER" sh -c "rm -f '$TMP_DEST' || true"

if [[ $EXIT_CODE -ne 0 ]]; then
  echo "Restore failed with exit code $EXIT_CODE"
  exit $EXIT_CODE
fi

echo "Restore completed successfully."

if [[ "$DO_RESTART" == "true" ]]; then
  echo "Restarting compose stack (docker compose up -d)..."
  $COMPOSE up -d
fi

echo "Done."