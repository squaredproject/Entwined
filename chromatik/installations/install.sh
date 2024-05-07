#!/usr/bin/env bash

echo "making fixtures from directory: $1 WILL REMOVE EXISTING ENTWINED FIXTURES"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

I="$HOME/Chromatik"
# Use this for WSL testing
# I="/mnt/c/Users/bbulk/Chromatik"

mkdir -p $I/Fixtures/Entwined
[ -d $I/Projects ] || mkdir -p $I/Projects
rm -rf $I/Fixtures/Entwined/*
rm -f $I/autoplay.lxr $I/Projects/entwined.lxp $I/config.json

echo "building LXF files from JSON descriptions"
if [ -f $1/shrubs.json ]
then
  python3 shrub.py --config $1/shrubs.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/fairy_circles.json ]
then
  python3 fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/trees.json ]
then
  python3 tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/bench.json ]
then
  python3 bench.py --config $1/bench.json --fixtures_folder $I/Fixtures/Entwined
fi

if [ -f $1/spots.json ]
then
  python3 spot.py --config $1/spots.json --fixtures_folder $I/Fixtures/Entwined
fi

if [[ -f $1/elder_mother_cubes.csv ]]; then
    python3 elder_mother.py --ndb_config $1/elder_ndb_ips.txt --cubes_config $1/elder_mother_cubes.csv --elder_config $1/elder_mother.json --fixtures_folder $I/Fixtures/Entwined/
    echo "Created elder mother fixtures"
fi

cp $1/*.lxp $I/Projects
if [ -f $1/config.json ]; then
  cp $1/config.json $I
fi

cp $1/*.lxr $I 2>/dev/null

if [[ -e $1/Fixtures ]]; then
  cp -r $1/Fixtures/* $I/Fixtures
fi

if [[ -e $1/Models ]]; then
  mkdir -p $I/Models
  cp -r $1/Models/* $I/Models
fi

# this one is independant of the installation
mkdir -p $I/Videos
# it is common to have subsequent copies complain on an overwite. Ignore the error.s
cp ../videos/* $I/Videos 2>/dev/null

# move ddptest_ndb_ips.txt tp ddbptest folder and rename
if [[ -f $1/ddptest_ndb_ips.txt ]]; then
    cp $1/ddptest_ndb_ips.txt  ../../ddptest/ndb_ips.txt
fi

