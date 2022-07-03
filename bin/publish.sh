#!/usr/bin/env bash
set -eEuo pipefail
set -x
cd "$(dirname "$0")"
cd ..

source .env
./gradlew publish
