#!/usr/bin/env bash

echo "making fixtures from directory: $1 WILL REMOVE EXISTING ENTWINED FIXTURES"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

I="~/Chromatik"

mkdir -p $I/Fixtures/Entwined
rm -rf $I/Fixtures/Entwined/*
rm -f $I/autoplay.lxr $I/Projects/entwined.lxp $I/config.json


if test -f "${1}/custom.sh"; then
    $1/custom.sh $1
fi

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $I/Fixtures/Entwined
python shrub.py --config $1/shrubs.json --fixtures_folder $I/Fixtures/Entwined
python tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder $I/Fixtures/Entwined
cp $1/entwined.lxp $I/Projects
cp $1/config.json $I

[[ -e $1/autoplay.lxr ]] && cp $1/autoplay.lxr $I
