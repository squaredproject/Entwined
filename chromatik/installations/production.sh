#!/usr/bin/env bash

echo "making prodution pi..."

if [[ -f $1/custom.sh ]]; then
    $1/custom.sh $1
fi

