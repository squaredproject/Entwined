#!/usr/bin/env bash

echo "installing from directory: $1 (remove existing), and installing crontab: statuscake and ddptest etc"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

./install.sh $1

if test -f "${1}/custom.sh"; then
    echo "installing custom configuration (statuscake ddptest whatever)"
    $1/custom.sh $1
fi

sudo systemctl restart chromatik
