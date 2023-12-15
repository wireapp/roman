#!/usr/bin/env sh

DOCKER_BUILDKIT=1 docker build --network host --target export-stage --output backend/target/reports -f Dockerfile.UnitTests .