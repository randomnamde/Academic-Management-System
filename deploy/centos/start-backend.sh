#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/app.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE"
  echo "Copy $SCRIPT_DIR/app.env.example to $SCRIPT_DIR/app.env and fill in the values first."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

APP_NAME="${APP_NAME:-student-management}"
APP_HOME="${APP_HOME:-/opt/student-management}"
APP_JAR="${APP_JAR:-$APP_HOME/backend/student-management.jar}"
JAVA_BIN="${JAVA_BIN:-/usr/bin/java}"
PID_FILE="${PID_FILE:-$APP_HOME/run/$APP_NAME.pid}"
APP_LOG_FILE="${APP_LOG_FILE:-$APP_HOME/logs/$APP_NAME.log}"
CONSOLE_LOG_FILE="${CONSOLE_LOG_FILE:-$APP_HOME/logs/console.out}"
FILE_UPLOAD_DIR="${FILE_UPLOAD_DIR:-$APP_HOME/uploads}"
SERVER_PORT="${SERVER_PORT:-8080}"
SERVER_CONTEXT_PATH="${SERVER_CONTEXT_PATH:-/api}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

mkdir -p "$(dirname "$PID_FILE")" "$(dirname "$APP_LOG_FILE")" "$(dirname "$CONSOLE_LOG_FILE")" "$FILE_UPLOAD_DIR"

if [[ ! -x "$JAVA_BIN" ]]; then
  echo "Java not found or not executable: $JAVA_BIN"
  exit 1
fi

if [[ ! -f "$APP_JAR" ]]; then
  echo "Jar not found: $APP_JAR"
  exit 1
fi

if [[ -f "$PID_FILE" ]]; then
  OLD_PID="$(cat "$PID_FILE")"
  if [[ -n "$OLD_PID" ]] && kill -0 "$OLD_PID" >/dev/null 2>&1; then
    echo "$APP_NAME is already running. PID=$OLD_PID"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

read -r -a JAVA_OPTS_ARRAY <<< "${JAVA_OPTS:-}"

CMD=(
  "$JAVA_BIN"
  "${JAVA_OPTS_ARRAY[@]}"
  -jar
  "$APP_JAR"
  "--spring.profiles.active=$SPRING_PROFILES_ACTIVE"
  "--server.port=$SERVER_PORT"
  "--server.servlet.context-path=$SERVER_CONTEXT_PATH"
)

nohup "${CMD[@]}" >> "$CONSOLE_LOG_FILE" 2>&1 &
APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

sleep 2
if ! kill -0 "$APP_PID" >/dev/null 2>&1; then
  echo "Failed to start $APP_NAME. Check $CONSOLE_LOG_FILE"
  rm -f "$PID_FILE"
  exit 1
fi

echo "$APP_NAME started successfully."
echo "PID: $APP_PID"
echo "URL: http://$(hostname -I | awk '{print $1}'):$SERVER_PORT$SERVER_CONTEXT_PATH"
echo "Console log: $CONSOLE_LOG_FILE"
