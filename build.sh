#!/bin/bash

set -e 


if [[ "${CI}" = "true" ]]; then
    rm -f ./config.yml
fi




mvn -B clean install
if [[ "${CI}" = "true" ]]; then
docker buildx build .  --platform linux/amd64,linux/arm64 --push --tag ghcr.io/if6was9/bq:latest
else
docker buildx build .  --platform linux/arm64,linux/amd64 --tag ghcr.io/if6was9/bq:latest
fi
