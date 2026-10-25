#!/usr/bin/env bash

set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
test_database_dir="$(mktemp -d "${TMPDIR:-/tmp}/vetnutri-test-db.XXXXXX")"

cleanup() {
    rm -rf "$test_database_dir"
}
trap cleanup EXIT INT TERM

echo "VetNutriMP — base temporaire : $test_database_dir"
echo "Cette base sera supprimée à la fermeture de l'application."

cd "$project_dir"
# VETNUTRI_ALLOW_DATABASE_DIR_OVERRIDE=1 est requis : hors tâches de test Gradle,
# Database.kt ignore VETNUTRI_TEST_DATABASE_DIR sans cette confirmation explicite.
VETNUTRI_TEST_DATABASE_DIR="$test_database_dir" \
VETNUTRI_ALLOW_DATABASE_DIR_OVERRIDE=1 \
./gradlew :composeApp:run
