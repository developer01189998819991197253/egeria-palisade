#!/usr/bin/env bash
docker-compose --no-ansi -f "../example-docker-services/docker-compose.yml" -p example up -d --build
