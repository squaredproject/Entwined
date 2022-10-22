#!/bin/bash

echo "capturing necessary install files into directory $1"

if [[ -d $1 ]]
then
	echo "directory exists, try again"
	exit
fi

echo "directory exists, copying files"

mkdir -p $1

cp ../Trees/Config.java $1
cp ../Trees/data/entwinedNDBs.json $1
cp ../Trees/data/entwinedTrees.json $1
cp ../Trees/data/entwinedCubes.json $1
cp ../Trees/data/entwinedShrubs.json $1
cp ../Trees/data/entwinedShrubCubes.json $1
cp ../Trees/data/entwinedFairyCircles.json $1
cp ../Trees/data/entwinedSpots.json $1

