#!/usr/bin/env sh

DOCKER_BUILDKIT=1 docker build -f Dockerfile.UnitTests . --network host