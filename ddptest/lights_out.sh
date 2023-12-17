#!/bin/bash

sudo service chromatik stop
#sudo nmap  -sn 10.0.0.100-255 -oG - | awk '/Up$/{print $2}' > /home/entwined/Entwined/ddptest/ndb_ips.txt

if [ -z "$1" ]; then
	$1 = "black"
fi

xs="1 2 3"
for x in $xs; do
	echo LIGHTS OUT $x	
	cat ndb_ips.txt | while read ip; do
	python3 ddptest.py --pattern $1 --leds 480 --host $ip
done
sleep 5
done
