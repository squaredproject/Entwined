#!/usr/bin/env bash

echo "making fixtures from directory: $1 WILL REMOVE EXISTING ENTWINED FIXTURES"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

I="$HOME/Chromatik"

mkdir -p $I/Fixtures/Entwined
rm -rf $I/Fixtures/Entwined/*
rm -f $I/autoplay.lxr $I/Projects/entwined.lxp $I/config.json

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $I/Fixtures/Entwined
python shrub.py --config $1/shrubs.json --fixtures_folder $I/Fixtures/Entwined
python tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder $I/Fixtures/Entwined
python bench.py --config $1/bench.json --fixtures_folder $I/Fixtures/Entwined

# remove this line when the following line is shown to work
if [[ -f $1/elder_mother_cubes.csv ]]; then
    python elder_mother.py --ndb_config $1/elder_ndb_ips.txt --cubes_config $1/elder_mother_cubes.csv --fixtures_folder $I/Fixtures/Entwined/
    echo "Created elder mother fixtures"
fi

cp $1/*.lxp $I/Projects
if [ -f $1/config.json ]; then
  cp $1/config.json $I
fi

[[ -e $1/autoplay.lxr ]] && cp $1/autoplay.lxr $I
