#!/usr/bin/env bash
set -eEuo pipefail
set -x
cd "$(dirname "$0")"
cd ..

docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 sonarqube:alpine
echo http://localhost:9000
