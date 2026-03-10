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
SERVER_PORT="${SERVER_PORT:-8080}"
SERVER_CONTEXT_PATH="${SERVER_CONTEXT_PATH:-/api}"

if [[ ! -f "$PID_FILE" ]]; then
  echo "$APP_NAME is not running."
  exit 1
fi

APP_PID="$(cat "$PID_FILE")"
if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" >/dev/null 2>&1; then
  echo "$APP_NAME is running. PID=$APP_PID"
  echo "Local URL: http://127.0.0.1:$SERVER_PORT$SERVER_CONTEXT_PATH"
  exit 0
fi

echo "$APP_NAME is not running, but PID file exists."
exit 1
