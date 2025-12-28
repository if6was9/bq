#!/bin/bash

set -e 


if [[ "${CI}" = "true" ]]; then
    rm -f ./config.yml
fi

mkdir -p target/stage
mkdir -p target/stage/lib


./mvnw -B clean install

cp target/*.jar target/stage/lib

docker build . -t bqsh

if [[ "${CI}" = "true" ]]; then
docker buildx build .  --platform linux/amd64,linux/arm64 --push --tag ghcr.io/if6was9/bqsh:latest
else
docker buildx build .  --platform linux/arm64,linux/amd64 --tag ghcr.io/if6was9/bqsh:latest
fi
