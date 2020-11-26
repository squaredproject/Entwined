#!/bin/sh

cd "$( dirname "$0" )"
exec java -Xms256m -Xmx1g -cp "Trees/build-tmp:Trees/code/*" RunHeadless "${PWD}/Trees"
