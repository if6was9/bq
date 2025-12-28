#!/bin/bash



if [[ "$1" = "bash" ]]; then
    exec bash
fi

if [[ "$1" = "bq" ]]; then
    exec bq
fi


exec bq "$@"



