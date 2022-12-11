#!/usr/bin/env bash

echo "installing from directory: $1 (remove existing), and installing crontab: statuscake and ddptest etc"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

D="~/Chromatik"

mkdir -p $D/Fixtures/Entwined
rm -rf $D/Fixtures/Entwined/*
rm -f $D/autoplay.lxr $D/Projects/entwined.lxp $D/config.json

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $D/Fixtures/Entwined
python shrub.py --config $1/shrubs.json --fixtures_folder $D/Fixtures/Entwined
python tree.py --tree_config $1/trees.json --branch_config $D/tree_branches.csv --fixtures_folder $D/Fixtures/Entwined
cp $1/entwined.lxp $D/Projects

[[ -e $1/autoplay.lxr ]] && cp $1/autoplay.lxr $D

if test -f "${1}/custom.sh"; then
    echo "installing custom configuration (statuscake ddptest whatever)"
    $1/custom.sh $1
fi

sudo systemctl restart chromatik
