#!/usr/bin/env bash

echo "making fixtures from directory: $1 WILL OVERWRITE EXISTING ENTWINED FIXTURES"

if [[ ! -d $1 ]]
then
        echo "$1 directory does not exist, try again"
        exit
fi

I="$HOME/Chromatik"
# Use this for WSL testing
# I="/mnt/c/Users/bbulk/Chromatik"

# clean and remove the old things
mkdir -p $I/Fixtures/Entwined
[ -d $I/Projects ] || mkdir -p $I/Projects
rm -rf $I/Fixtures/Entwined/*
rm -f $I/autoplay.lxr $I/Projects/entwined.lxp $I/config.json

# deal with the major annoyance that is python best practices across
# Macos Linux Windows

if [ ! -z ${2} ];
then
  PY=$2
  echo "Python specified on the command line, using $PY"
else  
  # use python as the default
  PY="python"
  # but override with python3 if available
  if command -v python3 &> /dev/null ; then
    echo "python3 detected, using that instead of python"
    PY="python3"
  fi
fi

# but if there's really no python, fail out
if command -v $PY &> /dev/null ; then

  echo "building LXF files from JSON descriptions using $PY"
  if [ -f $1/shrubs.json ]
  then
    $PY shrub.py --config $1/shrubs.json --fixtures_folder $I/Fixtures/Entwined
  fi

  if [ -f $1/fairy_circles.json ]
  then
    $PY fairy_circle.py --config $1/fairy_circles.json --fixtures_folder $I/Fixtures/Entwined
  fi

  if [ -f $1/trees.json ]
  then
    $PY tree.py --tree_config $1/trees.json --branch_config $1/tree_branches.csv --fixtures_folder $I/Fixtures/Entwined
  fi

  if [ -f $1/bench.json ]
  then
    $PY bench.py --config $1/bench.json --fixtures_folder $I/Fixtures/Entwined
  fi

  if [ -f $1/spots.json ]
  then
    $PY spot.py --config $1/spots.json --fixtures_folder $I/Fixtures/Entwined
  fi

  if [[ -f $1/elder_mother_cubes.csv ]]; then
      python3 elder_mother.py --ndb_config $1/elder_ndb_ips.txt --cubes_config $1/elder_mother_cubes.csv --elder_config $1/elder_mother.json --fixtures_folder $I/Fixtures/Entwined/
      echo "Created elder mother fixtures"
  fi

else
  echo "No python detected, skipping building fixture files from json descriptions"
fi

cp $1/*.lxp $I/Projects
if [ -f $1/config.json ]; then
  cp $1/config.json $I
fi

# autoplay is read from the root but the program looks in projects by default
cp $1/autoplay.lxr $I 2>/dev/null
cp $1/*.lxr $I/Projects 2>/dev/null

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

