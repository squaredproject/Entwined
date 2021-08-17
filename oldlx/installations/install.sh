#!/bin/bash

echo "installing from directory $1"

if [[ ! -d $1 ]]
then
	echo "directory does not exist, try again"
	exit
fi

echo "directory exists, copying files, remember to recompile"
cp $1/Config.java ../Trees
cp $1/entwined* ../Trees/data
