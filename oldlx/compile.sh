#!/bin/sh

cd "$( dirname "$0" )"
mkdir -p Trees/build-tmp
javac -cp "Trees/code/*" -d Trees/build-tmp Trees/*.java
