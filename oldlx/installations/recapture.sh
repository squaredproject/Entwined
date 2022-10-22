#!/bin/bash

if [[ ! -d $1 ]]
then
	echo "no directory with name $1, recapture requires a directory"
	exit
fi

echo "recapturing necessary install files into directory $1"
echo "overwriting old files"

cp ../Trees/Config.java $1
cp ../Trees/data/entwinedNDBs.json $1
cp ../Trees/data/entwinedTrees.json $1
cp ../Trees/data/entwinedCubes.json $1
cp ../Trees/data/entwinedShrubs.json $1
cp ../Trees/data/entwinedShrubCubes.json $1
cp ../Trees/data/entwinedFairyCircles.json $1
cp ../Trees/data/entwinedSpots.json $1

