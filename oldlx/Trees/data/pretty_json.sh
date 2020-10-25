#!/bin/bash

echo "cleaning $1"
cat $1 | python -m json.tool > $1.tmp
rm $1
mv $1.tmp $1
