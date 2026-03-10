#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
RELEASE_DIR="$PROJECT_ROOT/release/student-management"

echo "Preparing release directory: $RELEASE_DIR"
rm -rf "$RELEASE_DIR"
mkdir -p "$RELEASE_DIR/backend" "$RELEASE_DIR/frontend" "$RELEASE_DIR/database" "$RELEASE_DIR/scripts" "$RELEASE_DIR/nginx"

pushd "$PROJECT_ROOT/backend" >/dev/null
mvn -DskipTests clean package
JAR_FILE="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*original*.jar' | head -n 1)"
if [[ -z "$JAR_FILE" ]]; then
  echo "Cannot find packaged jar in backend/target"
  exit 1
fi
cp "$JAR_FILE" "$RELEASE_DIR/backend/student-management.jar"
popd >/dev/null

pushd "$PROJECT_ROOT/frontend" >/dev/null
npm ci
npm run build
cp -r dist/. "$RELEASE_DIR/frontend/"
popd >/dev/null

cp "$PROJECT_ROOT/database/schema.sql" "$RELEASE_DIR/database/"
cp "$PROJECT_ROOT/database/data.sql" "$RELEASE_DIR/database/"
cp "$SCRIPT_DIR/app.env.example" "$RELEASE_DIR/scripts/"
cp "$SCRIPT_DIR/start-backend.sh" "$RELEASE_DIR/scripts/"
cp "$SCRIPT_DIR/stop-backend.sh" "$RELEASE_DIR/scripts/"
cp "$SCRIPT_DIR/restart-backend.sh" "$RELEASE_DIR/scripts/"
cp "$SCRIPT_DIR/status-backend.sh" "$RELEASE_DIR/scripts/"
cp "$SCRIPT_DIR/student-management.conf" "$RELEASE_DIR/nginx/"
cp "$SCRIPT_DIR/student-management.service" "$RELEASE_DIR/scripts/"

echo "Release package is ready: $RELEASE_DIR"
