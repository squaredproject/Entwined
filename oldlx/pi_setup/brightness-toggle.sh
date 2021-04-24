#!/bin/sh

cd "$( dirname "$0" )"

while true
do
	python3 setbrightness.py --host 10.0.0.3 --brightness 0
	sleep 120
	python3 setbrightness.py --host 10.0.0.3 --brightness 100
	sleep 900
done

