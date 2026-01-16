#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./utility/reset_candles_cycle.sh [--no-backup] [--container NAME] [--compose-cmd "docker compose"]
#
# By default tries to use container name "stockviewer-postgres" and uses "docker compose" (fallback to docker-compose).
# Example:
#   ./utility/reset_candles_cycle.sh            # runs with backup
#   ./utility/reset_candles_cycle.sh --no-backup
#   ./utility/reset_candles_cycle.sh --container my-postgres

# Defaults (override with env or args)
CONTAINER_DEFAULT="stockviewer-postgres"
DB_USER_DEFAULT="${DB_USER:-user}"
DB_NAME_DEFAULT="${DB_NAME:-stockviewer}"
BACKUP_DIR_DEFAULT="./backups"
COMPOSE_CMD=""

DO_BACKUP=true
CONTAINER="$CONTAINER_DEFAULT"
DB_USER="$DB_USER_DEFAULT"
DB_NAME="$DB_NAME_DEFAULT"
BACKUP_DIR="$BACKUP_DIR_DEFAULT"

print_usage() {
  cat <<EOF
Usage: $0 [--no-backup] [--container NAME] [--compose-cmd "docker compose"|"docker-compose"]

Options:
  --no-backup           skip pg_dump backup before truncation
  --container NAME      postgres container name or id (default: $CONTAINER_DEFAULT)
  --compose-cmd CMD     exact compose command to use (default: auto-detect "docker compose" or "docker-compose")
  -h, --help            show this help
EOF
  exit 1
}

# parse args (simple)
while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-backup) DO_BACKUP=false; shift ;;
    --container) CONTAINER="$2"; shift 2 ;;
    --compose-cmd) COMPOSE_CMD="$2"; shift 2 ;;
    -h|--help) print_usage ;;
    *) echo "Unknown arg: $1"; print_usage ;;
  esac
done

# detect compose command
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

timestamp() { date +"%Y%m%d-%H%M%S"; }

wait_for_container_running() {
  local cid="$1"
  local tries=30
  local i
  for i in $(seq 1 $tries); do
    local running
    running=$(docker inspect -f '{{.State.Running}}' "$cid" 2>/dev/null || echo "false")
    if [[ "$running" == "true" ]]; then
      return 0
    fi
    sleep 1
  done
  return 1
}

wait_for_pg_ready() {
  local cid="$1"
  local tries=60
  local i
  echo "Waiting for Postgres readiness inside container '$cid'..."
  for i in $(seq 1 $tries); do
    if docker exec "$cid" sh -c "pg_isready -U '$DB_USER' -d '$DB_NAME' >/dev/null 2>&1"; then
      echo "Postgres is ready."
      return 0
    fi
    sleep 1
  done
  return 1
}

do_backup() {
  local cid="$1"
  mkdir -p "$BACKUP_DIR"
  local fn="$BACKUP_DIR/stock_candles_backup_$(timestamp).sql"
  echo "Backing up tables stock_data.candle and stock_data.indicator to: $fn"
  docker exec -i "$cid" pg_dump -U "$DB_USER" -d "$DB_NAME" -t stock_data.candle -t stock_data.indicator > "$fn"
  echo "Backup finished: $fn"
}

do_truncate() {
  local cid="$1"
  echo "Executing TRUNCATE on container $cid"
  docker exec -i "$cid" psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" -c "TRUNCATE TABLE stock_data.indicator, stock_data.candle RESTART IDENTITY;"
  echo "TRUNCATE completed."
}

echo "Step 1/7: shutting down docker compose services"
$COMPOSE down

echo "Step 2/7: docker compose up -d"
$COMPOSE up -d

echo "Detecting postgres container (default: $CONTAINER)..."
# if container exists as given, use it; otherwise try to find postgres container
if ! docker inspect "$CONTAINER" >/dev/null 2>&1; then
  echo "Container '$CONTAINER' not found, attempting to auto-detect postgres container..."
  # try service name, then ancestor image
  CONTAINER="$(docker ps --filter "name=postgres" --format '{{.Names}}' | head -n1 || true)"
  if [[ -z "$CONTAINER" ]]; then
    CONTAINER="$(docker ps --filter "ancestor=postgres" --format '{{.ID}}' | head -n1 || true)"
  fi
  if [[ -z "$CONTAINER" ]]; then
    echo "ERROR: Could not auto-detect postgres container. Provide --container <name>."
    exit 3
  fi
  echo "Auto-detected container: $CONTAINER"
fi

echo "Step 3/7: Waiting for container to be running..."
if ! wait_for_container_running "$CONTAINER"; then
  echo "ERROR: container $CONTAINER did not reach running state."
  exit 4
fi

echo "Step 4/7: waiting for Postgres readiness..."
if ! wait_for_pg_ready "$CONTAINER"; then
  echo "ERROR: Postgres not ready in container $CONTAINER"
  exit 5
fi

if [[ "$DO_BACKUP" == "true" ]]; then
  echo "Step 4.5/7: performing backup"
  do_backup "$CONTAINER"
else
  echo "Skipping backup (requested)"
fi

echo "Step 5/7: truncating candles & indicators"
do_truncate "$CONTAINER"

echo "Step 6/7: docker compose down"
$COMPOSE down

# echo "Step 7/7: docker compose up -d (bringing services back up)"
# $COMPOSE up -d

echo "Done. Postgres candles reset and compose restarted."