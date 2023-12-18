#!/usr/bin/env sh
set -x

echo "1/4) Starting test environment..."
docker-compose up -d db

echo "2/4) Running tests..."
DOCKER_BUILDKIT=1 docker build --network host --target export-stage --output backend/target/reports -f Dockerfile.UnitTests .

echo "3/4) Cleaning up test environment..."
docker-compose stop

echo "4/4) Evaluating tests result exit status..."
EXIT_CODE=`cat backend/target/reports/test.result`
exit $EXIT_CODE