#!/usr/bin/env bash

echo "making fixtures from directory: $1 WILL REMOVE EXISTING ENTWINED FIXTURES"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

I="$HOME/Chromatik"

mkdir -p $I/Fixtures/Entwined/Projects
[ -d $I/Projects ] || mkdir -p $I/Projects
rm -rf $I/Fixtures/Entwined/*
rm -f $I/autoplay.lxr $I/Projects/entwined.lxp $I/config.json

echo "building LXF files from JSON descriptions"
if [ -f $1/shrubs.json ]
then
  python shrub.py --config $1/shrubs.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/fairy_circles.json ]
then
  python fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/trees.json ]
then
  python tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/bench.json ]
then
  python bench.py --config $1/bench.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/spots.json ]
then
  python spot.py --config $1/spots.json --fixtures_folder $I/Fixtures/Entwined
fi

if [[ -f $1/elder_mother_cubes.csv ]]; then
    python elder_mother.py --ndb_config $1/elder_ndb_ips.txt --cubes_config $1/elder_mother_cubes.csv --elder_config $1/elder_mother.json --fixtures_folder $I/Fixtures/Entwined/
    echo "Created elder mother fixtures"
fi

cp $1/*.lxp $I/Projects
if [ -f $1/config.json ]; then
  cp $1/config.json $I
fi

[[ -e $1/autoplay.lxr ]] && cp $1/autoplay.lxr $I

# this one is independant of the installation
mkdir -p $I/Videos
cp ../videos/* $I/Videos

# move ddptest_ndb_ips.txt tp ddbptest folder and rename
if [[ -f $1/ddptest_ndb_ips.txt ]]; then
    cp $1/ddptest_ndb_ips.txt  ../../ddptest/ndb_ips.txt
fi

