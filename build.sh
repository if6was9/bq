#!/bin/bash

set -e 


if [[ "${CI}" = "true" ]]; then
    rm -f ./config.yml
fi

./mvnw -B clean test 


