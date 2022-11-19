#!/usr/bin/env bash

echo "installing from directory: $1 (remove existing), and installing crontab: statuscake and ddptest etc"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

mkdir -p ~/Chromatik/Fixtures/Entwined
rm ~/Chromatik/Fixtures/Entwined/*

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $1/fairy_circles.json --fixtures_folder ~/Chromatik/Fixtures/Entwined
python shrub.py --config $1/shrubs.json --fixtures_folder ~/Chromatik/Fixtures/Entwined
python tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder ~/Chromatik/Fixtures/Entwined
cp $1/entwined.lxp ~/Chromatik/Projects

if test -f "${1}/custom.sh"; then
    echo "installing custom configuration (statuscake ddptest whatever)"
    $1/custom.sh $1
fi
