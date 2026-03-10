#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/app.env}"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

APP_NAME="${APP_NAME:-student-management}"
APP_HOME="${APP_HOME:-/opt/student-management}"
PID_FILE="${PID_FILE:-$APP_HOME/run/$APP_NAME.pid}"

if [[ ! -f "$PID_FILE" ]]; then
  echo "No PID file found: $PID_FILE"
  exit 0
fi

APP_PID="$(cat "$PID_FILE")"

if [[ -z "$APP_PID" ]] || ! kill -0 "$APP_PID" >/dev/null 2>&1; then
  echo "Process already stopped."
  rm -f "$PID_FILE"
  exit 0
fi

kill "$APP_PID"

for _ in {1..30}; do
  if ! kill -0 "$APP_PID" >/dev/null 2>&1; then
    rm -f "$PID_FILE"
    echo "$APP_NAME stopped."
    exit 0
  fi
  sleep 1
done

kill -9 "$APP_PID"
rm -f "$PID_FILE"
echo "$APP_NAME force stopped."
